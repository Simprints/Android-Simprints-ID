package com.simprints.fingerprint.infra.scanner.capture

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.simprints.core.livedata.LiveDataEvent
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FingerprintScanningStatusTrackerTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var fingerprintScanningStatusTracker: FingerprintScanningStatusTracker
    private val scanCompletedObserver: Observer<LiveDataEvent> = mockk(relaxed = true)

    @Before
    fun setup() {
        fingerprintScanningStatusTracker = FingerprintScanningStatusTracker()
        fingerprintScanningStatusTracker.scanCompleted.observeForever(scanCompletedObserver)
    }

    @Test
    fun `notifyScanCompleted posts LiveDataEvent to scanCompleted`() {
        // Act
        fingerprintScanningStatusTracker.notifyScanCompleted()

        // Assert
        verify { scanCompletedObserver.onChanged(any()) }
    }
}
