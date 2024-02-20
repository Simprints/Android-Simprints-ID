package com.simprints.infra.sync

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.sync.SyncConstants.DEVICE_SYNC_WORK_NAME
import com.simprints.infra.sync.SyncConstants.DEVICE_SYNC_WORK_NAME_ONE_TIME
import com.simprints.infra.sync.SyncConstants.IMAGE_UP_SYNC_WORK_NAME
import com.simprints.infra.sync.SyncConstants.PROJECT_SYNC_WORK_NAME
import com.simprints.infra.sync.SyncConstants.RECORD_UPLOAD_INPUT_ID_NAME
import com.simprints.infra.sync.SyncConstants.RECORD_UPLOAD_INPUT_SUBJECT_IDS_NAME
import com.simprints.infra.sync.usecase.CleanupDeprecatedWorkersUseCase
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SyncOrchestratorImplTest {


    @MockK
    private lateinit var workManager: WorkManager

    @MockK
    private lateinit var authStore: AuthStore

    @MockK
    private lateinit var cleanupDeprecatedWorkers: CleanupDeprecatedWorkersUseCase

    private lateinit var syncOrchestrator: SyncOrchestratorImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        syncOrchestrator = SyncOrchestratorImpl(
            workManager,
            authStore,
            cleanupDeprecatedWorkers,
        )
    }

    @Test
    fun `does not schedules any workers if not logged in`() = runTest {
        every { authStore.signedInProjectId } returns ""

        syncOrchestrator.scheduleBackgroundWork()

        verify(exactly = 0) {
            workManager.enqueueUniquePeriodicWork(any(), any(), any())
        }
    }

    @Test
    fun `schedules all necessary background workers if logged in`() = runTest {
        every { authStore.signedInProjectId } returns "projectId"

        syncOrchestrator.scheduleBackgroundWork()

        verify {
            workManager.enqueueUniquePeriodicWork(PROJECT_SYNC_WORK_NAME, any(), any())
            workManager.enqueueUniquePeriodicWork(DEVICE_SYNC_WORK_NAME, any(), any())
            workManager.enqueueUniquePeriodicWork(IMAGE_UP_SYNC_WORK_NAME, any(), any())
        }
    }

    @Test
    fun `cancels all necessary background workers`() = runTest {
        syncOrchestrator.cancelBackgroundWork()

        verify {
            workManager.cancelUniqueWork(PROJECT_SYNC_WORK_NAME)
            workManager.cancelUniqueWork(DEVICE_SYNC_WORK_NAME)
            workManager.cancelUniqueWork(IMAGE_UP_SYNC_WORK_NAME)
        }
    }

    @Test
    fun `schedules device worker when requested`() = runTest {
        syncOrchestrator.startDeviceSync()

        verify {
            workManager.enqueueUniqueWork(
                DEVICE_SYNC_WORK_NAME_ONE_TIME,
                any(),
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun `reschedules image worker when requested`() = runTest {
        syncOrchestrator.rescheduleImageUpSync()

        verify {
            workManager.enqueueUniquePeriodicWork(
                IMAGE_UP_SYNC_WORK_NAME,
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
                        RECORD_UPLOAD_INPUT_SUBJECT_IDS_NAME
                    )
                    val instructionIdInput = oneTimeWorkRequest.workSpec.input.getString(
                        RECORD_UPLOAD_INPUT_ID_NAME
                    )
                    instructionIdInput == INSTRUCTION_ID &&
                        subjectIdsInput.contentEquals(arrayOf(SUBJECT_ID))
                }
            )
        }
    }

    @Test
    fun `delegates worker cleanup requests`() = runTest {
        syncOrchestrator.cleanupWorkers()
        verify { cleanupDeprecatedWorkers.invoke() }
    }

    companion object {

        private const val INSTRUCTION_ID = "id"
        private const val SUBJECT_ID = "subjectId"
    }
}
