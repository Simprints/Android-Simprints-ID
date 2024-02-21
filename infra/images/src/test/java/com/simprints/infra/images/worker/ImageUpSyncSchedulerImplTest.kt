package com.simprints.infra.images.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.simprints.infra.config.store.ConfigRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*

import org.junit.Before
import org.junit.Test

internal class ImageUpSyncSchedulerImplTest {

    @MockK
    private lateinit var ctx: Context

    @MockK
    private lateinit var workManager: WorkManager

    @MockK
    private lateinit var configRepository: ConfigRepository

    private lateinit var scheduler: ImageUpSyncSchedulerImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(ctx) } returns workManager

        scheduler = ImageUpSyncSchedulerImpl(ctx, configRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `should schedule worker to upload on any network`() = runTest {
        coEvery { configRepository.getProjectConfiguration().synchronization.up.imagesRequireUnmeteredConnection } returns false

        scheduler.scheduleImageUpSync()

        coVerify {
            workManager.enqueueUniquePeriodicWork(
                any(),
                any(),
                match<PeriodicWorkRequest> {
                    it.workSpec.constraints.requiredNetworkType == NetworkType.CONNECTED
                }
            )
        }
    }

    @Test
    fun `should schedule worker to upload only on unmetered network`() = runTest {
        coEvery { configRepository.getProjectConfiguration().synchronization.up.imagesRequireUnmeteredConnection } returns true

        scheduler.scheduleImageUpSync()

        coVerify {
            workManager.enqueueUniquePeriodicWork(
                any(),
                any(),
                match<PeriodicWorkRequest> {
                    it.workSpec.constraints.requiredNetworkType == NetworkType.UNMETERED
                }
            )
        }
    }

    @Test
    fun `should cancel and reschedule worker`() = runTest {
        coEvery { configRepository.getProjectConfiguration().synchronization.up.imagesRequireUnmeteredConnection } returns true

        scheduler.rescheduleImageUpSync()

        coVerify {
            workManager.enqueueUniquePeriodicWork(
                any(),
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                any()
            )
        }
    }

    @Test
    fun `should cancel  worker`() = runTest {
        scheduler.cancelImageUpSync()

        coVerify {
            workManager.cancelUniqueWork(any())
        }
    }

}
