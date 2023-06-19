package com.simprints.infra.config.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class ConfigurationSchedulerImplTest {

    private val ctx = mockk<Context>()
    private val workManager = mockk<WorkManager>(relaxed = true)
    private lateinit var configurationSchedulerImpl: ConfigurationSchedulerImpl

    @Before
    fun setup() {
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(ctx) } returns workManager

        configurationSchedulerImpl = ConfigurationSchedulerImpl(ctx)
    }

    @Test
    fun `scheduleSync should schedule the worker`() {
        configurationSchedulerImpl.scheduleSync()

        verify {
            workManager.enqueueUniquePeriodicWork(
                ConfigurationSchedulerImpl.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                any(),
            )
        }
    }

    @Test
    fun `cancelSync should cancel the worker`() {
        configurationSchedulerImpl.cancelScheduledSync()

        verify { workManager.cancelUniqueWork(ConfigurationSchedulerImpl.WORK_NAME) }
    }
}
