package com.simprints.fingerprint.infra.scanner.data.worker

import androidx.work.WorkManager
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verifySequence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FirmwareFileUpdateSchedulerTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val workManagerMock = mockk<WorkManager>()
    private val fingerprintConfiguration = mockk<FingerprintConfiguration>()
    private val configRepository = mockk<ConfigRepository> {
        coEvery { getProjectConfiguration() } returns mockk {
            every { fingerprint } returns fingerprintConfiguration
        }
    }

    private val firmwareFileUpdateScheduler =
        FirmwareFileUpdateScheduler(
            mockk(),
            configRepository,
            CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
        )

    @Before
    fun setUp() {
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(any()) } returns workManagerMock
    }

    @Test
    fun projectIsOnVero2Only_schedulesWork() = runTest {
        every { fingerprintConfiguration.allowedScanners } returns listOf(
            FingerprintConfiguration.VeroGeneration.VERO_2
        )
        every { workManagerMock.enqueueUniquePeriodicWork(any(), any(), any()) } returns mockk()

        firmwareFileUpdateScheduler.scheduleOrCancelWorkIfNecessary()

        verifySequence { workManagerMock.enqueueUniquePeriodicWork(any(), any(), any()) }
    }

    @Test
    fun projectIsBothVero1AndVero2_schedulesWork() = runTest {
        every { fingerprintConfiguration.allowedScanners } returns listOf(
            FingerprintConfiguration.VeroGeneration.VERO_1,
            FingerprintConfiguration.VeroGeneration.VERO_2
        )
        every { workManagerMock.enqueueUniquePeriodicWork(any(), any(), any()) } returns mockk()

        firmwareFileUpdateScheduler.scheduleOrCancelWorkIfNecessary()

        verifySequence { workManagerMock.enqueueUniquePeriodicWork(any(), any(), any()) }
    }

    @Test
    fun projectIsOnVero1Only_cancelsScheduledWork() = runTest {
        every { fingerprintConfiguration.allowedScanners } returns listOf(
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
