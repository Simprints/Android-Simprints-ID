package com.simprints.fingerprint.infra.scanner.data.worker

import androidx.work.WorkManager
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.config.store.models.FingerprintConfiguration
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verifySequence
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class FirmwareFileUpdateSchedulerTest {

    private val workManagerMock = mockk<WorkManager>()
    private val fingerprintConfiguration = mockk<FingerprintConfiguration>()
    private val configManager = mockk<ConfigManager> {
        coEvery { getProjectConfiguration() } returns mockk {
            every { fingerprint } returns fingerprintConfiguration
        }
    }

    private val firmwareFileUpdateScheduler =
        FirmwareFileUpdateScheduler(mockk(), configManager)

    @Before
    fun setUp() {
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(any()) } returns workManagerMock
    }

    @Test
    fun projectIsOnVero2Only_schedulesWork() = runTest {
        every { fingerprintConfiguration.allowedVeroGenerations } returns listOf(
            FingerprintConfiguration.VeroGeneration.VERO_2
        )
        every { workManagerMock.enqueueUniquePeriodicWork(any(), any(), any()) } returns mockk()

        firmwareFileUpdateScheduler.scheduleOrCancelWorkIfNecessary()

        verifySequence { workManagerMock.enqueueUniquePeriodicWork(any(), any(), any()) }
    }

    @Test
    fun projectIsBothVero1AndVero2_schedulesWork() = runTest {
        every { fingerprintConfiguration.allowedVeroGenerations } returns listOf(
            FingerprintConfiguration.VeroGeneration.VERO_1,
            FingerprintConfiguration.VeroGeneration.VERO_2
        )
        every { workManagerMock.enqueueUniquePeriodicWork(any(), any(), any()) } returns mockk()

        firmwareFileUpdateScheduler.scheduleOrCancelWorkIfNecessary()

        verifySequence { workManagerMock.enqueueUniquePeriodicWork(any(), any(), any()) }
    }

    @Test
    fun projectIsOnVero1Only_cancelsScheduledWork() = runTest {
        every { fingerprintConfiguration.allowedVeroGenerations } returns listOf(
            FingerprintConfiguration.VeroGeneration.VERO_1
        )
        every { workManagerMock.cancelUniqueWork(any()) } returns mockk()

        firmwareFileUpdateScheduler.scheduleOrCancelWorkIfNecessary()

        verifySequence { workManagerMock.cancelUniqueWork(any()) }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}
