package com.simprints.infra.config.sync

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.simprints.infra.config.sync.ProjectConfigurationSchedulerImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class ProjectConfigurationSchedulerImplTest {

    private val ctx = mockk<Context>()
    private val workManager = mockk<WorkManager>(relaxed = true)
    private lateinit var projectConfigSchedulerImpl: ProjectConfigurationSchedulerImpl

    @Before
    fun setup() {
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(ctx) } returns workManager

        projectConfigSchedulerImpl =
            ProjectConfigurationSchedulerImpl(ctx)
    }

    @Test
    fun `scheduleSync should schedule the worker`() {
        projectConfigSchedulerImpl.scheduleSync()

        verify {
            workManager.enqueueUniquePeriodicWork(
                ProjectConfigurationSchedulerImpl.WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                any(),
            )
        }
    }

    @Test
    fun `cancelSync should cancel the worker`() {
        projectConfigSchedulerImpl.cancelScheduledSync()

        verify { workManager.cancelUniqueWork(ProjectConfigurationSchedulerImpl.WORK_NAME) }
    }
}
