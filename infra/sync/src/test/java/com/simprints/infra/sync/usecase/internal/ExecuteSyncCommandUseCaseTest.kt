package com.simprints.infra.sync.usecase.internal

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.eventsync.sync.master.EventSyncMasterWorker
import com.simprints.infra.sync.ExecutableSyncCommand
import com.simprints.infra.sync.SyncCommands
import com.simprints.infra.sync.SyncConstants.DEVICE_SYNC_WORK_NAME
import com.simprints.infra.sync.SyncConstants.EVENT_SYNC_WORK_NAME
import com.simprints.infra.sync.SyncConstants.EVENT_SYNC_WORK_NAME_ONE_TIME
import com.simprints.infra.sync.SyncConstants.FILE_UP_SYNC_WORK_NAME
import com.simprints.infra.sync.SyncConstants.FIRMWARE_UPDATE_WORK_NAME
import com.simprints.infra.sync.SyncConstants.PROJECT_SYNC_WORK_NAME
import com.simprints.infra.sync.firmware.ShouldScheduleFirmwareUpdateUseCase
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

class ExecuteSyncCommandUseCaseTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var workManager: WorkManager

    @MockK
    private lateinit var authStore: AuthStore

    @MockK
    private lateinit var configRepository: ConfigRepository

    @MockK
    private lateinit var eventSyncManager: EventSyncManager

    @MockK
    private lateinit var shouldScheduleFirmwareUpdate: ShouldScheduleFirmwareUpdateUseCase

    private lateinit var useCase: ExecuteSyncCommandUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = createUseCase()
    }

    @Test
    fun `does not schedules any workers if not logged in`() = runTest {
        every { authStore.signedInProjectId } returns ""
        coEvery { shouldScheduleFirmwareUpdate.invoke() } returns false

        useCase(executable(SyncCommands.Schedule.Everything.start())).join()

        verify(exactly = 0) { workManager.enqueueUniquePeriodicWork(any(), any(), any()) }
    }

    @Test
    fun `schedules all necessary background workers if logged in`() = runTest {
        every { authStore.signedInProjectId } returns "projectId"
        coEvery { shouldScheduleFirmwareUpdate.invoke() } returns true

        useCase(executable(SyncCommands.Schedule.Everything.start())).join()

        verify {
            workManager.enqueueUniquePeriodicWork(PROJECT_SYNC_WORK_NAME, any(), any())
            workManager.enqueueUniquePeriodicWork(DEVICE_SYNC_WORK_NAME, any(), any())
            workManager.enqueueUniquePeriodicWork(FILE_UP_SYNC_WORK_NAME, any(), any())
            workManager.enqueueUniquePeriodicWork(EVENT_SYNC_WORK_NAME, any(), any())
            workManager.enqueueUniquePeriodicWork(FIRMWARE_UPDATE_WORK_NAME, any(), any())
        }
    }

    @Test
    fun `schedules images with any connection if not specified`() = runTest {
        coEvery {
            configRepository
                .getProjectConfiguration()
                .synchronization.up.simprints.imagesRequireUnmeteredConnection
        } returns false
        every { authStore.signedInProjectId } returns "projectId"

        useCase(executable(SyncCommands.Schedule.Everything.start())).join()

        verify {
            workManager.enqueueUniquePeriodicWork(
                FILE_UP_SYNC_WORK_NAME,
                any(),
                match { it.workSpec.constraints.requiredNetworkType == NetworkType.CONNECTED },
            )
        }
    }

    @Test
    fun `schedules images with unmetered constraint if requested`() = runTest {
        coEvery {
            configRepository
                .getProjectConfiguration()
                .synchronization.up.simprints.imagesRequireUnmeteredConnection
        } returns true
        every { authStore.signedInProjectId } returns "projectId"
        coEvery { shouldScheduleFirmwareUpdate.invoke() } returns false

        useCase(executable(SyncCommands.Schedule.Everything.start())).join()

        verify {
            workManager.enqueueUniquePeriodicWork(
                FILE_UP_SYNC_WORK_NAME,
                any(),
                match { it.workSpec.constraints.requiredNetworkType == NetworkType.UNMETERED },
            )
        }
    }

    @Test
    fun `cancels firmware update worker if firmware update should not be scheduled`() = runTest {
        every { authStore.signedInProjectId } returns "projectId"
        coEvery { shouldScheduleFirmwareUpdate.invoke() } returns false

        useCase(executable(SyncCommands.Schedule.Everything.start())).join()

        verify { workManager.cancelUniqueWork(FIRMWARE_UPDATE_WORK_NAME) }
    }

    @Test
    fun `cancels all necessary background workers`() = runTest {
        every { eventSyncManager.getAllWorkerTag() } returns "syncWorkers"

        useCase(executable(SyncCommands.Schedule.Everything.stop()))

        verify {
            workManager.cancelUniqueWork(PROJECT_SYNC_WORK_NAME)
            workManager.cancelUniqueWork(DEVICE_SYNC_WORK_NAME)
            workManager.cancelUniqueWork(FILE_UP_SYNC_WORK_NAME)
            workManager.cancelUniqueWork(EVENT_SYNC_WORK_NAME)
            workManager.cancelUniqueWork(EVENT_SYNC_WORK_NAME_ONE_TIME)
            workManager.cancelUniqueWork(FIRMWARE_UPDATE_WORK_NAME)
            workManager.cancelAllWorkByTag("syncWorkers")
        }
    }

    @Test
    fun `reschedules event sync worker with correct tags`() = runTest {
        every { eventSyncManager.getPeriodicWorkTags() } returns listOf("tag1", "tag2")

        useCase(executable(SyncCommands.Schedule.Events.start())).join()

        verify {
            workManager.enqueueUniquePeriodicWork(
                EVENT_SYNC_WORK_NAME,
                any(),
                match { it.tags.containsAll(setOf("tag1", "tag2")) },
            )
        }
    }

    @Test
    fun `reschedules event sync worker with correct delay`() = runTest {
        every { eventSyncManager.getPeriodicWorkTags() } returns listOf("tag1", "tag2")

        useCase(executable(SyncCommands.Schedule.Events.start(withDelay = true))).join()

        verify {
            workManager.enqueueUniquePeriodicWork(
                EVENT_SYNC_WORK_NAME,
                any(),
                match { it.workSpec.initialDelay > 0 },
            )
        }
    }

    @Test
    fun `stop and start schedule events routes to cancel and reschedule with delay`() = runTest {
        every { eventSyncManager.getAllWorkerTag() } returns "syncWorkers"
        every { eventSyncManager.getPeriodicWorkTags() } returns listOf("tag1", "tag2")

        useCase(executable(SyncCommands.Schedule.Events.stopAndStart(withDelay = true))).join()

        verify {
            workManager.cancelUniqueWork(EVENT_SYNC_WORK_NAME)
            workManager.cancelUniqueWork(EVENT_SYNC_WORK_NAME_ONE_TIME)
            workManager.cancelAllWorkByTag("syncWorkers")
            workManager.enqueueUniquePeriodicWork(
                EVENT_SYNC_WORK_NAME,
                any(),
                match {
                    it.workSpec.initialDelay > 0 &&
                        it.tags.containsAll(setOf("tag1", "tag2"))
                },
            )
        }
    }

    @Test
    fun `cancel event sync worker cancels correct worker`() = runTest {
        every { eventSyncManager.getAllWorkerTag() } returns "syncWorkers"

        useCase(executable(SyncCommands.Schedule.Events.stop()))

        verify {
            workManager.cancelUniqueWork(EVENT_SYNC_WORK_NAME)
            workManager.cancelUniqueWork(EVENT_SYNC_WORK_NAME_ONE_TIME)
            workManager.cancelAllWorkByTag("syncWorkers")
        }
    }

    @Test
    fun `start event sync worker with correct tags`() = runTest {
        every { eventSyncManager.getOneTimeWorkTags() } returns listOf("tag1", "tag2")

        useCase(executable(SyncCommands.OneTime.Events.start())).join()

        verify {
            workManager.enqueueUniqueWork(
                EVENT_SYNC_WORK_NAME_ONE_TIME,
                any(),
                match<OneTimeWorkRequest> { it.tags.containsAll(setOf("tag1", "tag2")) },
            )
        }
    }

    @Test
    fun `start event sync worker with correct input data`() = runTest {
        every { eventSyncManager.getOneTimeWorkTags() } returns listOf("tag1", "tag2")

        useCase(executable(SyncCommands.OneTime.Events.start(isDownSyncAllowed = false))).join()

        verify {
            workManager.enqueueUniqueWork(
                EVENT_SYNC_WORK_NAME_ONE_TIME,
                any(),
                match<OneTimeWorkRequest> {
                    !it.workSpec.input.getBoolean(EventSyncMasterWorker.IS_DOWN_SYNC_ALLOWED, true)
                },
            )
        }
    }

    @Test
    fun `stop and start one-time event sync routes to stop and start with expected input param`() = runTest {
        every { eventSyncManager.getAllWorkerTag() } returns "syncWorkers"
        every { eventSyncManager.getOneTimeWorkTags() } returns listOf("tag1", "tag2")

        useCase(executable(SyncCommands.OneTime.Events.stopAndStart(isDownSyncAllowed = false))).join()

        verify {
            workManager.cancelUniqueWork(EVENT_SYNC_WORK_NAME_ONE_TIME)
            workManager.cancelAllWorkByTag("syncWorkers")
            workManager.enqueueUniqueWork(
                EVENT_SYNC_WORK_NAME_ONE_TIME,
                any(),
                match<OneTimeWorkRequest> {
                    it.tags.containsAll(setOf("tag1", "tag2")) &&
                        !it.workSpec.input.getBoolean(EventSyncMasterWorker.IS_DOWN_SYNC_ALLOWED, true)
                },
            )
        }
    }

    @Test
    fun `stop event sync worker cancels correct workers`() = runTest {
        every { eventSyncManager.getAllWorkerTag() } returns "syncWorkers"

        useCase(executable(SyncCommands.OneTime.Events.stop()))

        verify {
            workManager.cancelUniqueWork(EVENT_SYNC_WORK_NAME_ONE_TIME)
            workManager.cancelAllWorkByTag("syncWorkers")
        }
    }

    @Test
    fun `reschedules image worker when requested`() = runTest {
        useCase(executable(SyncCommands.Schedule.Images.start())).join()

        verify {
            workManager.enqueueUniquePeriodicWork(
                FILE_UP_SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                any(),
            )
        }
    }

    @Test
    fun `start image sync re-starts image worker`() = runTest {
        useCase(executable(SyncCommands.OneTime.Images.start())).join()

        verify {
            workManager.cancelUniqueWork(FILE_UP_SYNC_WORK_NAME)
            workManager.enqueueUniqueWork(
                FILE_UP_SYNC_WORK_NAME,
                any(),
                any<OneTimeWorkRequest>(),
            )
        }
    }

    @Test
    fun `stop image sync cancels image worker`() = runTest {
        useCase(executable(SyncCommands.OneTime.Images.stop()))

        verify { workManager.cancelUniqueWork(FILE_UP_SYNC_WORK_NAME) }
    }

    @Test
    fun `invoke stop command returns completed job and routes to stop logic`() = runTest {
        val job = useCase(executable(SyncCommands.Schedule.Images.stop()))

        assertThat(job.isCompleted).isTrue()
        verify { workManager.cancelUniqueWork(FILE_UP_SYNC_WORK_NAME) }
    }

    @Test
    fun `stop and start around runs block before starting`() = runTest {
        val blockStarted = Channel<Unit>(Channel.UNLIMITED)
        val unblock = Channel<Unit>(Channel.UNLIMITED)
        val block: suspend () -> Unit = {
            blockStarted.trySend(Unit)
            unblock.receive()
        }

        val job = useCase(executable(SyncCommands.Schedule.Images.stopAndStartAround(block)))

        verify {
            workManager.cancelUniqueWork(FILE_UP_SYNC_WORK_NAME)
        }

        blockStarted.receive()

        verify(exactly = 0) {
            workManager.enqueueUniquePeriodicWork(
                FILE_UP_SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                any(),
            )
        }

        unblock.trySend(Unit)
        job.join()

        verify {
            workManager.enqueueUniquePeriodicWork(
                FILE_UP_SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                any(),
            )
        }
    }

    @Test
    fun `stops image worker when event sync starts`() = runTest { // init block test
        val eventStartFlow = MutableSharedFlow<List<WorkInfo>>()
        every { workManager.getWorkInfosFlow(any()) } returns eventStartFlow

        useCase = createUseCase()

        eventStartFlow.emit(createWorkInfo(WorkInfo.State.RUNNING))

        verify {
            workManager.enqueueUniquePeriodicWork(
                FILE_UP_SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                any(),
            )
        }
    }

    @Test
    fun `does not stop image worker when event sync is not running`() = runTest { // init block test
        val eventStartFlow = MutableSharedFlow<List<WorkInfo>>()
        every { workManager.getWorkInfosFlow(any()) } returns eventStartFlow

        useCase = createUseCase()

        eventStartFlow.emit(createWorkInfo(WorkInfo.State.CANCELLED))

        verify(exactly = 0) {
            workManager.enqueueUniquePeriodicWork(
                FILE_UP_SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                any(),
            )
        }
    }

    private fun executable(syncCommand: com.simprints.infra.sync.SyncCommand) = syncCommand as ExecutableSyncCommand

    private fun createUseCase() = ExecuteSyncCommandUseCase(
        workManager = workManager,
        authStore = authStore,
        configRepository = configRepository,
        eventSyncManager = eventSyncManager,
        shouldScheduleFirmwareUpdate = shouldScheduleFirmwareUpdate,
        appScope = CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
    )

    private fun createWorkInfo(state: WorkInfo.State) = listOf(
        WorkInfo(UUID.randomUUID(), state, emptySet()),
    )
}
