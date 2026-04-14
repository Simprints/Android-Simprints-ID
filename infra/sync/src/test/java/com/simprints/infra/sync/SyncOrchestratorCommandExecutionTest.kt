package com.simprints.infra.sync

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.eventsync.DeleteSyncInfoUseCase
import com.simprints.infra.eventsync.EventSyncWorkerTagRepository
import com.simprints.infra.eventsync.sync.down.DownSyncStateProcessor
import com.simprints.infra.eventsync.sync.up.UpSyncStateProcessor
import com.simprints.infra.sync.SyncConstants.DEVICE_SYNC_WORK_NAME
import com.simprints.infra.sync.SyncConstants.EVENT_DOWN_SYNC_WORK_NAME
import com.simprints.infra.sync.SyncConstants.EVENT_DOWN_SYNC_WORK_NAME_ONE_TIME
import com.simprints.infra.sync.SyncConstants.EVENT_UP_SYNC_WORK_NAME
import com.simprints.infra.sync.SyncConstants.EVENT_UP_SYNC_WORK_NAME_ONE_TIME
import com.simprints.infra.sync.SyncConstants.FILE_UP_SYNC_WORK_NAME
import com.simprints.infra.sync.SyncConstants.FIRMWARE_UPDATE_WORK_NAME
import com.simprints.infra.sync.SyncConstants.PROJECT_SYNC_WORK_NAME
import com.simprints.infra.sync.firmware.ShouldScheduleFirmwareUpdateUseCase
import com.simprints.infra.sync.usecase.CleanupDeprecatedWorkersUseCase
import com.simprints.infra.sync.usecase.internal.ObserveImageSyncStatusUseCase
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

class SyncOrchestratorCommandExecutionTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var workManager: WorkManager

    @MockK
    private lateinit var authStore: AuthStore

    @MockK
    private lateinit var configRepository: ConfigRepository

    @MockK
    private lateinit var deleteSyncInfo: DeleteSyncInfoUseCase

    @MockK
    private lateinit var eventSyncWorkerTagRepository: EventSyncWorkerTagRepository

    @MockK
    private lateinit var upSyncStateProcessor: UpSyncStateProcessor

    @MockK
    private lateinit var downSyncStateProcessor: DownSyncStateProcessor

    @MockK
    private lateinit var observeImageSyncStatus: ObserveImageSyncStatusUseCase

    @MockK
    private lateinit var shouldScheduleFirmwareUpdate: ShouldScheduleFirmwareUpdateUseCase

    @MockK
    private lateinit var cleanupDeprecatedWorkers: CleanupDeprecatedWorkersUseCase

    @MockK
    private lateinit var imageSyncTimestampProvider: ImageSyncTimestampProvider

    private lateinit var orchestrator: SyncOrchestratorImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { workManager.getWorkInfosFlow(any()) } returns flowOf(emptyList())
        orchestrator = createOrchestrator()
    }

    @Test
    fun `does not schedule any workers if not logged in`() = runTest {
        every { authStore.signedInProjectId } returns ""
        coEvery { shouldScheduleFirmwareUpdate.invoke() } returns false

        orchestrator.execute(ScheduleCommand.Everything.reschedule()).join()

        verify(exactly = 0) { workManager.enqueueUniquePeriodicWork(any(), any(), any()) }
    }

    @Test
    fun `schedules all necessary background workers if logged in`() = runTest {
        every { authStore.signedInProjectId } returns "projectId"
        coEvery { shouldScheduleFirmwareUpdate.invoke() } returns true

        orchestrator.execute(ScheduleCommand.Everything.reschedule()).join()

        verify {
            workManager.enqueueUniquePeriodicWork(PROJECT_SYNC_WORK_NAME, any(), any())
            workManager.enqueueUniquePeriodicWork(DEVICE_SYNC_WORK_NAME, any(), any())
            workManager.enqueueUniquePeriodicWork(FILE_UP_SYNC_WORK_NAME, any(), any())
            workManager.enqueueUniquePeriodicWork(EVENT_UP_SYNC_WORK_NAME, any(), any())
            workManager.enqueueUniquePeriodicWork(EVENT_DOWN_SYNC_WORK_NAME, any(), any())
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

        orchestrator.execute(ScheduleCommand.Everything.reschedule()).join()

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

        orchestrator.execute(ScheduleCommand.Everything.reschedule()).join()

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

        orchestrator.execute(ScheduleCommand.Everything.reschedule()).join()

        verify { workManager.cancelUniqueWork(FIRMWARE_UPDATE_WORK_NAME) }
    }

    @Test
    fun `unschedule cancels all necessary background workers`() = runTest {
        every { eventSyncWorkerTagRepository.getAllWorkerTag() } returns "syncWorkers"

        orchestrator.execute(ScheduleCommand.Everything.unschedule())

        verify {
            workManager.cancelUniqueWork(PROJECT_SYNC_WORK_NAME)
            workManager.cancelUniqueWork(DEVICE_SYNC_WORK_NAME)
            workManager.cancelUniqueWork(FILE_UP_SYNC_WORK_NAME)
            workManager.cancelUniqueWork(EVENT_UP_SYNC_WORK_NAME)
            workManager.cancelUniqueWork(EVENT_UP_SYNC_WORK_NAME_ONE_TIME)
            workManager.cancelUniqueWork(EVENT_DOWN_SYNC_WORK_NAME)
            workManager.cancelUniqueWork(EVENT_DOWN_SYNC_WORK_NAME_ONE_TIME)
            workManager.cancelUniqueWork(FIRMWARE_UPDATE_WORK_NAME)
        }
    }

    @Test
    fun `reschedules up sync worker with correct tags`() = runTest {
        every { eventSyncWorkerTagRepository.getUpSyncPeriodicWorkTags() } returns listOf("tag1", "tag2")

        orchestrator.execute(ScheduleCommand.UpSync.reschedule()).join()

        verify {
            workManager.enqueueUniquePeriodicWork(
                EVENT_UP_SYNC_WORK_NAME,
                any(),
                match { it.tags.containsAll(setOf("tag1", "tag2")) },
            )
        }
    }

    @Test
    fun `reschedules up sync worker with correct delay`() = runTest {
        every { eventSyncWorkerTagRepository.getUpSyncPeriodicWorkTags() } returns listOf("tag1", "tag2")

        orchestrator.execute(ScheduleCommand.UpSync.reschedule(withDelay = true)).join()

        verify {
            workManager.enqueueUniquePeriodicWork(
                EVENT_UP_SYNC_WORK_NAME,
                any(),
                match { it.workSpec.initialDelay > 0 },
            )
        }
    }

    @Test
    fun `unschedule up sync cancels correct workers`() = runTest {
        every { eventSyncWorkerTagRepository.getUpSyncAllWorkersTag() } returns "upSyncWorkers"

        orchestrator.execute(ScheduleCommand.UpSync.unschedule())

        verify {
            workManager.cancelUniqueWork(EVENT_UP_SYNC_WORK_NAME)
            workManager.cancelUniqueWork(EVENT_UP_SYNC_WORK_NAME_ONE_TIME)
            workManager.cancelAllWorkByTag("upSyncWorkers")
        }
    }

    @Test
    fun `reschedules down sync worker with correct tags`() = runTest {
        every { eventSyncWorkerTagRepository.getDownSyncPeriodicWorkTags() } returns listOf("tag3", "tag4")

        orchestrator.execute(ScheduleCommand.DownSync.reschedule()).join()

        verify {
            workManager.enqueueUniquePeriodicWork(
                EVENT_DOWN_SYNC_WORK_NAME,
                any(),
                match { it.tags.containsAll(setOf("tag3", "tag4")) },
            )
        }
    }

    @Test
    fun `unschedule down sync cancels correct workers`() = runTest {
        every { eventSyncWorkerTagRepository.getDownSyncAllWorkersTag() } returns "downSyncWorkers"

        orchestrator.execute(ScheduleCommand.DownSync.unschedule())

        verify {
            workManager.cancelUniqueWork(EVENT_DOWN_SYNC_WORK_NAME)
            workManager.cancelUniqueWork(EVENT_DOWN_SYNC_WORK_NAME_ONE_TIME)
            workManager.cancelAllWorkByTag("downSyncWorkers")
        }
    }

    @Test
    fun `start one-time up sync uses correct tags`() = runTest {
        every { eventSyncWorkerTagRepository.getUpSyncOneTimeWorkTags() } returns listOf("tag1", "tag2")

        orchestrator.execute(OneTime.UpSync.start()).join()

        verify {
            workManager.enqueueUniqueWork(
                EVENT_UP_SYNC_WORK_NAME_ONE_TIME,
                any(),
                match<OneTimeWorkRequest> { it.tags.containsAll(setOf("tag1", "tag2")) },
            )
        }
    }

    @Test
    fun `restart one-time up sync routes to stop and start`() = runTest {
        every { eventSyncWorkerTagRepository.getUpSyncAllWorkersTag() } returns "upSyncWorkers"
        every { eventSyncWorkerTagRepository.getUpSyncOneTimeWorkTags() } returns listOf("tag1", "tag2")

        orchestrator.execute(OneTime.UpSync.restart()).join()

        verify {
            workManager.cancelUniqueWork(EVENT_UP_SYNC_WORK_NAME_ONE_TIME)
            workManager.cancelAllWorkByTag("upSyncWorkers")
            workManager.enqueueUniqueWork(
                EVENT_UP_SYNC_WORK_NAME_ONE_TIME,
                any(),
                match<OneTimeWorkRequest> { it.tags.containsAll(setOf("tag1", "tag2")) },
            )
        }
    }

    @Test
    fun `stop one-time up sync cancels correct workers`() = runTest {
        every { eventSyncWorkerTagRepository.getUpSyncAllWorkersTag() } returns "upSyncWorkers"

        orchestrator.execute(OneTime.UpSync.stop())

        verify {
            workManager.cancelUniqueWork(EVENT_UP_SYNC_WORK_NAME_ONE_TIME)
            workManager.cancelAllWorkByTag("upSyncWorkers")
        }
    }

    @Test
    fun `start one-time down sync uses correct tags`() = runTest {
        every { eventSyncWorkerTagRepository.getDownSyncOneTimeWorkTags() } returns listOf("tag3", "tag4")

        orchestrator.execute(OneTime.DownSync.start()).join()

        verify {
            workManager.enqueueUniqueWork(
                EVENT_DOWN_SYNC_WORK_NAME_ONE_TIME,
                any(),
                match<OneTimeWorkRequest> { it.tags.containsAll(setOf("tag3", "tag4")) },
            )
        }
    }

    @Test
    fun `stop one-time down sync cancels correct workers`() = runTest {
        every { eventSyncWorkerTagRepository.getDownSyncAllWorkersTag() } returns "downSyncWorkers"

        orchestrator.execute(OneTime.DownSync.stop())

        verify {
            workManager.cancelUniqueWork(EVENT_DOWN_SYNC_WORK_NAME_ONE_TIME)
            workManager.cancelAllWorkByTag("downSyncWorkers")
        }
    }

    @Test
    fun `reschedules image worker when requested`() = runTest {
        orchestrator.execute(ScheduleCommand.Images.reschedule()).join()

        verify {
            workManager.enqueueUniquePeriodicWork(
                FILE_UP_SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                any(),
            )
        }
    }

    @Test
    fun `start one-time image sync re-starts image worker`() = runTest {
        orchestrator.execute(OneTime.Images.start()).join()

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
    fun `stop one-time image sync cancels image worker`() = runTest {
        orchestrator.execute(OneTime.Images.stop())

        verify { workManager.cancelUniqueWork(FILE_UP_SYNC_WORK_NAME) }
    }

    @Test
    fun `unschedule images returns completed job and routes to stop logic`() = runTest {
        val job = orchestrator.execute(ScheduleCommand.Images.unschedule())

        assertThat(job.isCompleted).isTrue()
        verify { workManager.cancelUniqueWork(FILE_UP_SYNC_WORK_NAME) }
    }

    @Test
    fun `reschedules image worker when event sync starts`() = runTest {
        val eventStartFlow = MutableSharedFlow<List<WorkInfo>>()
        every { workManager.getWorkInfosFlow(any()) } returns eventStartFlow

        orchestrator = createOrchestrator()

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
    fun `does not reschedule image worker when event sync is not running`() = runTest {
        val eventStartFlow = MutableSharedFlow<List<WorkInfo>>()
        every { workManager.getWorkInfosFlow(any()) } returns eventStartFlow

        orchestrator = createOrchestrator()

        eventStartFlow.emit(createWorkInfo(WorkInfo.State.CANCELLED))

        verify(exactly = 0) {
            workManager.enqueueUniquePeriodicWork(
                FILE_UP_SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                any(),
            )
        }
    }

    private fun createOrchestrator() = SyncOrchestratorImpl(
        workManager = workManager,
        authStore = authStore,
        configRepository = configRepository,
        deleteSyncInfo = deleteSyncInfo,
        eventSyncWorkerTagRepository = eventSyncWorkerTagRepository,
        upSyncStateProcessor = upSyncStateProcessor,
        downSyncStateProcessor = downSyncStateProcessor,
        observeImageSyncStatus = observeImageSyncStatus,
        shouldScheduleFirmwareUpdate = shouldScheduleFirmwareUpdate,
        cleanupDeprecatedWorkers = cleanupDeprecatedWorkers,
        imageSyncTimestampProvider = imageSyncTimestampProvider,
        appScope = CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
        ioDispatcher = testCoroutineRule.testCoroutineDispatcher,
    )

    private fun createWorkInfo(state: WorkInfo.State) = listOf(
        WorkInfo(UUID.randomUUID(), state, emptySet()),
    )
}
