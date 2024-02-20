package com.simprints.infra.sync

import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.sync.SyncConstants.DEVICE_SYNC_WORK_NAME
import com.simprints.infra.sync.SyncConstants.DEVICE_SYNC_WORK_NAME_ONE_TIME
import com.simprints.infra.sync.SyncConstants.PROJECT_SYNC_WORK_NAME
import io.mockk.MockKAnnotations
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

    private lateinit var syncOrchestrator: SyncOrchestratorImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        syncOrchestrator = SyncOrchestratorImpl(
            workManager,
            authStore,
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
        }
    }

    @Test
    fun `cancels all necessary background workers`() = runTest {
        syncOrchestrator.cancelBackgroundWork()

        verify {
            workManager.cancelUniqueWork(PROJECT_SYNC_WORK_NAME)
            workManager.cancelUniqueWork(DEVICE_SYNC_WORK_NAME)
        }
    }

    @Test
    fun `schedules device worker when requested`() = runTest {
        syncOrchestrator.startDeviceSync()

        verify {
            workManager.enqueueUniqueWork(DEVICE_SYNC_WORK_NAME_ONE_TIME, any(), any<OneTimeWorkRequest>())
        }
    }

}
