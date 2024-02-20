package com.simprints.infra.sync.config

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.simprints.infra.sync.config.ProjectConfigurationSchedulerImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class ProjectConfigurationSchedulerImplTest {

    private val ctx = mockk<Context>()
    private val workManager = mockk<WorkManager>(relaxed = true)
    private lateinit var configurationSchedulerImpl: ProjectConfigurationSchedulerImpl

    @Before
    fun setup() {
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(ctx) } returns workManager

        configurationSchedulerImpl =
            ProjectConfigurationSchedulerImpl(ctx)
    }

    @Test
    fun `scheduleProjectSync should schedule the worker`() {
        configurationSchedulerImpl.scheduleProjectSync()

        verify {
            workManager.enqueueUniquePeriodicWork(
                ProjectConfigurationSchedulerImpl.PROJECT_SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                any(),
            )
        }
    }

    @Test
    fun `cancelProjectSync should cancel the worker`() {
        configurationSchedulerImpl.cancelProjectSync()

        verify { workManager.cancelUniqueWork(ProjectConfigurationSchedulerImpl.PROJECT_SYNC_WORK_NAME) }
    }

    @Test
    fun `startDeviceSync should schedule the worker`() {
        configurationSchedulerImpl.startDeviceSync()

        verify {
            workManager.enqueueUniqueWork(
                ProjectConfigurationSchedulerImpl.DEVICE_SYNC_WORK_NAME_ONE_TIME,
                ExistingWorkPolicy.KEEP,
                any<OneTimeWorkRequest>(),
            )
        }
    }

    @Test
    fun `scheduleDeviceSync should schedule the worker`() {
        configurationSchedulerImpl.scheduleDeviceSync()

        verify {
            workManager.enqueueUniquePeriodicWork(
                ProjectConfigurationSchedulerImpl.DEVICE_SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                any(),
            )
        }
    }

    @Test
    fun `cancelDeviceSync should cancel the worker`() {
        configurationSchedulerImpl.cancelDeviceSync()

        verify { workManager.cancelUniqueWork(ProjectConfigurationSchedulerImpl.DEVICE_SYNC_WORK_NAME) }
    }
}
