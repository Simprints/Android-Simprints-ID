package com.simprints.infra.sync

import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.sync.SyncConstants.DEVICE_SYNC_WORK_NAME_ONE_TIME
import com.simprints.infra.sync.SyncConstants.PROJECT_SYNC_WORK_NAME_ONE_TIME
import com.simprints.infra.sync.SyncConstants.RECORD_UPLOAD_INPUT_ID_NAME
import com.simprints.infra.sync.SyncConstants.RECORD_UPLOAD_INPUT_SUBJECT_IDS_NAME
import com.simprints.infra.sync.usecase.CleanupDeprecatedWorkersUseCase
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
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
    private lateinit var eventSyncManager: EventSyncManager

    @MockK
    private lateinit var cleanupDeprecatedWorkers: CleanupDeprecatedWorkersUseCase

    @MockK
    private lateinit var imageSyncTimestampProvider: ImageSyncTimestampProvider

    private lateinit var syncOrchestrator: SyncOrchestratorImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        syncOrchestrator = createSyncOrchestrator()
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
        verify { workManager.pruneWork() }
        verify { imageSyncTimestampProvider.clearTimestamp() }
    }

    @Test
    fun `delegates worker cleanup requests`() = runTest {
        syncOrchestrator.cleanupWorkers()
        verify { cleanupDeprecatedWorkers.invoke() }
    }

    private fun createSyncOrchestrator() = SyncOrchestratorImpl(
        workManager,
        eventSyncManager,
        cleanupDeprecatedWorkers,
        imageSyncTimestampProvider,
    )

    private fun createWorkInfo(state: WorkInfo.State) = listOf(
        WorkInfo(UUID.randomUUID(), state, emptySet()),
    )

    companion object {
        private const val INSTRUCTION_ID = "id"
        private const val SUBJECT_ID = "subjectId"
    }
}
