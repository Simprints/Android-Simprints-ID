package com.simprints.infra.config.sync

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
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
    fun `scheduleSync should schedule the worker`() {
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
    fun `cancelSync should cancel the worker`() {
        configurationSchedulerImpl.cancelProjectSync()

        verify { workManager.cancelUniqueWork(ProjectConfigurationSchedulerImpl.PROJECT_SYNC_WORK_NAME) }
    }
}
