package com.simprints.fingerprint.scanner.data.worker

import androidx.work.WorkManager
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.scanner.domain.ScannerGeneration
import io.mockk.*
import org.junit.Before
import org.junit.Test

class FirmwareFileUpdateSchedulerTest {

    private val workManagerMock = mockk<WorkManager>()
    private val preferencesManagerMock = mockk<FingerprintPreferencesManager>()

    private val firmwareFileUpdateScheduler = FirmwareFileUpdateScheduler(mockk(), preferencesManagerMock)

    @Before
    fun setUp() {
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(any()) } returns workManagerMock
    }

    @Test
    fun projectIsOnVero2Only_schedulesWork() {
        every { preferencesManagerMock.scannerGenerations } returns listOf(ScannerGeneration.VERO_2)
        every { workManagerMock.enqueueUniquePeriodicWork(any(), any(), any()) } returns mockk()

        firmwareFileUpdateScheduler.scheduleOrCancelWorkIfNecessary()

        verifySequence { workManagerMock.enqueueUniquePeriodicWork(any(), any(), any()) }
    }

    @Test
    fun projectIsBothVero1AndVero2_schedulesWork() {
        every { preferencesManagerMock.scannerGenerations } returns listOf(ScannerGeneration.VERO_1, ScannerGeneration.VERO_2)
        every { workManagerMock.enqueueUniquePeriodicWork(any(), any(), any()) } returns mockk()

        firmwareFileUpdateScheduler.scheduleOrCancelWorkIfNecessary()

        verifySequence { workManagerMock.enqueueUniquePeriodicWork(any(), any(), any()) }
    }

    @Test
    fun projectIsOnVero1Only_cancelsScheduledWork() {
        every { preferencesManagerMock.scannerGenerations } returns listOf(ScannerGeneration.VERO_1)
        every { workManagerMock.cancelUniqueWork(any()) } returns mockk()

        firmwareFileUpdateScheduler.scheduleOrCancelWorkIfNecessary()

        verifySequence { workManagerMock.cancelUniqueWork(any()) }
    }
}
