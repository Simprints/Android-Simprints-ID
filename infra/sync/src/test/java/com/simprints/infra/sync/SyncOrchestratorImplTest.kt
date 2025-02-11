package com.simprints.infra.sync

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.common.truth.Truth.assertThat
import com.google.common.util.concurrent.ListenableFuture
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.sync.SyncConstants.DEVICE_SYNC_WORK_NAME
import com.simprints.infra.sync.SyncConstants.DEVICE_SYNC_WORK_NAME_ONE_TIME
import com.simprints.infra.sync.SyncConstants.EVENT_SYNC_WORK_NAME
import com.simprints.infra.sync.SyncConstants.EVENT_SYNC_WORK_NAME_ONE_TIME
import com.simprints.infra.sync.SyncConstants.FILE_UP_SYNC_WORK_NAME
import com.simprints.infra.sync.SyncConstants.FIRMWARE_UPDATE_WORK_NAME
import com.simprints.infra.sync.SyncConstants.PROJECT_SYNC_WORK_NAME
import com.simprints.infra.sync.SyncConstants.PROJECT_SYNC_WORK_NAME_ONE_TIME
import com.simprints.infra.sync.SyncConstants.RECORD_UPLOAD_INPUT_ID_NAME
import com.simprints.infra.sync.SyncConstants.RECORD_UPLOAD_INPUT_SUBJECT_IDS_NAME
import com.simprints.infra.sync.firmware.ShouldScheduleFirmwareUpdateUseCase
import com.simprints.infra.sync.usecase.CleanupDeprecatedWorkersUseCase
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

class SyncOrchestratorImplTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var workManager: WorkManager

    @MockK
    private lateinit var authStore: AuthStore

    @MockK
    private lateinit var configManager: ConfigManager

    @MockK
    private lateinit var eventSyncManager: EventSyncManager

    @MockK
    private lateinit var shouldScheduleFirmwareUpdate: ShouldScheduleFirmwareUpdateUseCase

    @MockK
    private lateinit var cleanupDeprecatedWorkers: CleanupDeprecatedWorkersUseCase

    private lateinit var syncOrchestrator: SyncOrchestratorImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        syncOrchestrator = createSyncOrchestrator()
    }

    @Test
    fun `does not schedules any workers if not logged in`() = runTest {
        every { authStore.signedInProjectId } returns ""
        coEvery { shouldScheduleFirmwareUpdate.invoke() } returns false

        syncOrchestrator.scheduleBackgroundWork()

        verify(exactly = 0) {
            workManager.enqueueUniquePeriodicWork(any(), any(), any())
        }
    }

    @Test
    fun `schedules all necessary background workers if logged in`() = runTest {
        every { authStore.signedInProjectId } returns "projectId"
        coEvery { shouldScheduleFirmwareUpdate.invoke() } returns true

        syncOrchestrator.scheduleBackgroundWork()

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
            configManager
                .getProjectConfiguration()
                .synchronization.up.simprints.imagesRequireUnmeteredConnection
        } returns false
        every { authStore.signedInProjectId } returns "projectId"

        syncOrchestrator.scheduleBackgroundWork()

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
            configManager
                .getProjectConfiguration()
                .synchronization.up.simprints.imagesRequireUnmeteredConnection
        } returns true
        every { authStore.signedInProjectId } returns "projectId"
        coEvery { shouldScheduleFirmwareUpdate.invoke() } returns false

        syncOrchestrator.scheduleBackgroundWork()

        verify {
            workManager.enqueueUniquePeriodicWork(
                FILE_UP_SYNC_WORK_NAME,
                any(),
                match { it.workSpec.constraints.requiredNetworkType == NetworkType.UNMETERED },
            )
        }
    }

    @Test
    fun `schedules cancel firmware update worker if no support for vero 2`() = runTest {
        every { authStore.signedInProjectId } returns "projectId"
        coEvery { shouldScheduleFirmwareUpdate.invoke() } returns false

        syncOrchestrator.scheduleBackgroundWork()

        verify {
            workManager.cancelUniqueWork(FIRMWARE_UPDATE_WORK_NAME)
        }
    }

    @Test
    fun `cancels all necessary background workers`() = runTest {
        every { eventSyncManager.getAllWorkerTag() } returns "syncWorkers"

        syncOrchestrator.cancelBackgroundWork()

        verify {
            workManager.cancelUniqueWork(PROJECT_SYNC_WORK_NAME)
            workManager.cancelUniqueWork(DEVICE_SYNC_WORK_NAME)
            workManager.cancelUniqueWork(FILE_UP_SYNC_WORK_NAME)
            workManager.cancelUniqueWork(EVENT_SYNC_WORK_NAME)
            workManager.cancelUniqueWork(FIRMWARE_UPDATE_WORK_NAME)

            // Explicitly cancel event sync sub-workers
            workManager.cancelAllWorkByTag("syncWorkers")
        }
    }

    @Test
    fun `schedules device worker when refresh requested`() = runTest {
        syncOrchestrator.refreshConfiguration()

        verify {
            workManager.enqueueUniqueWork(
                DEVICE_SYNC_WORK_NAME_ONE_TIME,
                any(),
                any<OneTimeWorkRequest>(),
            )
        }
        verify {
            workManager.enqueueUniqueWork(
                PROJECT_SYNC_WORK_NAME_ONE_TIME,
                any(),
                any<OneTimeWorkRequest>(),
            )
        }
    }

    @Test
    fun `configuration refresh emits when workers are complete`() = runTest {
        val eventStartFlow = flowOf(
            createWorkInfo(WorkInfo.State.ENQUEUED),
            createWorkInfo(WorkInfo.State.RUNNING),
            createWorkInfo(WorkInfo.State.SUCCEEDED),
        )
        every { workManager.getWorkInfosFlow(any()) } returns eventStartFlow

        // Should only emit the success
        assertThat(syncOrchestrator.refreshConfiguration().count()).isEqualTo(1)
    }

    @Test
    fun `reschedules event sync worker with correct tags`() = runTest {
        every { eventSyncManager.getPeriodicWorkTags() } returns listOf("tag1", "tag2")

        syncOrchestrator.rescheduleEventSync()

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

        syncOrchestrator.rescheduleEventSync(true)

        verify {
            workManager.enqueueUniquePeriodicWork(
                EVENT_SYNC_WORK_NAME,
                any(),
                match { it.workSpec.initialDelay > 0 },
            )
        }
    }

    @Test
    fun `cancel event sync worker cancels correct worker`() = runTest {
        every { eventSyncManager.getAllWorkerTag() } returns "syncWorkers"

        syncOrchestrator.cancelEventSync()

        verify {
            workManager.cancelUniqueWork(EVENT_SYNC_WORK_NAME)
            workManager.cancelUniqueWork(EVENT_SYNC_WORK_NAME_ONE_TIME)
            workManager.cancelAllWorkByTag("syncWorkers")
        }
    }

    @Test
    fun `start event sync worker with correct tags`() = runTest {
        every { eventSyncManager.getOneTimeWorkTags() } returns listOf("tag1", "tag2")

        syncOrchestrator.startEventSync()

        verify {
            workManager.enqueueUniqueWork(
                EVENT_SYNC_WORK_NAME_ONE_TIME,
                any(),
                match<OneTimeWorkRequest> { it.tags.containsAll(setOf("tag1", "tag2")) },
            )
        }
    }

    @Test
    fun `stop event sync worker cancels correct worker`() = runTest {
        every { eventSyncManager.getAllWorkerTag() } returns "syncWorkers"

        syncOrchestrator.cancelEventSync()

        verify {
            workManager.cancelUniqueWork(EVENT_SYNC_WORK_NAME_ONE_TIME)
            workManager.cancelAllWorkByTag("syncWorkers")
        }
    }

    @Test
    fun `reschedules image worker when requested`() = runTest {
        syncOrchestrator.rescheduleImageUpSync()

        verify {
            workManager.enqueueUniquePeriodicWork(
                FILE_UP_SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                any(),
            )
        }
    }

    @Test
    fun `schedules record upload`() = runTest {
        syncOrchestrator.uploadEnrolmentRecords(INSTRUCTION_ID, listOf(SUBJECT_ID))

        coVerify(exactly = 1) {
            workManager.enqueueUniqueWork(
                any(),
                any(),
                match<OneTimeWorkRequest> { oneTimeWorkRequest ->
                    val subjectIdsInput = oneTimeWorkRequest.workSpec.input.getStringArray(
                        RECORD_UPLOAD_INPUT_SUBJECT_IDS_NAME,
                    )
                    val instructionIdInput = oneTimeWorkRequest.workSpec.input.getString(
                        RECORD_UPLOAD_INPUT_ID_NAME,
                    )
                    instructionIdInput == INSTRUCTION_ID &&
                        subjectIdsInput.contentEquals(arrayOf(SUBJECT_ID))
                },
            )
        }
    }

    @Test
    fun `delegates sync info deletion`() = runTest {
        syncOrchestrator.deleteEventSyncInfo()
        coVerify { eventSyncManager.deleteSyncInfo() }
    }

    @Test
    fun `delegates worker cleanup requests`() = runTest {
        syncOrchestrator.cleanupWorkers()
        verify { cleanupDeprecatedWorkers.invoke() }
    }

    @Test
    fun `stops image worker when event sync starts`() = runTest {
        val eventStartFlow = MutableSharedFlow<List<WorkInfo>>()
        every { workManager.getWorkInfosFlow(any()) } returns eventStartFlow
        every {
            workManager.getWorkInfosForUniqueWork(FILE_UP_SYNC_WORK_NAME)
        } returns mockFuture(createWorkInfo(WorkInfo.State.RUNNING))

        // Recreating orchestrator with new mocks since the subscription is done in init
        syncOrchestrator = createSyncOrchestrator()
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
    fun `does not stop image worker when event sync is not running`() = runTest {
        val eventStartFlow = MutableSharedFlow<List<WorkInfo>>()
        every { workManager.getWorkInfosFlow(any()) } returns eventStartFlow

        // Recreating orchestrator with new mocks since the subscription is done in init
        syncOrchestrator = createSyncOrchestrator()
        eventStartFlow.emit(createWorkInfo(WorkInfo.State.CANCELLED))

        verify(exactly = 0) {
            workManager.getWorkInfosForUniqueWork(FILE_UP_SYNC_WORK_NAME)
            workManager.cancelWorkById(any())
        }
    }

    private fun createSyncOrchestrator() = SyncOrchestratorImpl(
        workManager,
        authStore,
        configManager,
        eventSyncManager,
        shouldScheduleFirmwareUpdate,
        cleanupDeprecatedWorkers,
        CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
    )

    private fun mockFuture(workInfo: List<WorkInfo>) = mockk<ListenableFuture<List<WorkInfo>>> { every { get() } returns workInfo }

    private fun createWorkInfo(state: WorkInfo.State) = listOf(
        WorkInfo(UUID.randomUUID(), state, emptySet()),
    )

    companion object {
        private const val INSTRUCTION_ID = "id"
        private const val SUBJECT_ID = "subjectId"
    }
}
