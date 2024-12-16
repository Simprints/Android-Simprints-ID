package com.simprints.fingerprint.infra.scanner.capture

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FingerprintScanningStatusTrackerTest {
    private lateinit var tracker: FingerprintScanningStatusTracker

    @Before
    fun setup() {
        tracker = FingerprintScanningStatusTracker(UnconfinedTestDispatcher())
    }

    @Test
    fun `startScanning emits Scanning state`() = runTest {
        val job = launch {
            tracker.state.collect { state ->
                assertThat(state).isEqualTo(FingerprintScanState.Scanning)
            }
        }
        tracker.startScanning()
        job.cancel()
    }

    @Test
    fun `completeScan emits ScanCompleted state`() = runTest {
        val job = launch {
            tracker.state.collect { state ->
                assertThat(state).isEqualTo(FingerprintScanState.ScanCompleted)
            }
        }
        tracker.completeScan()
        job.cancel()
    }

    @Test
    fun `setImageQualityCheckingResult emits Good state when quality is OK`() = runTest {
        val job = launch {
            tracker.state.collect { state ->
                assertThat(state).isEqualTo(FingerprintScanState.ImageQualityChecking.Good)
            }
        }
        tracker.setImageQualityCheckingResult(true)
        job.cancel()
    }

    @Test
    fun `setImageQualityCheckingResult emits Bad state when quality is not OK`() = runTest {
        val job = launch {
            tracker.state.collect { state ->
                assertThat(state).isEqualTo(FingerprintScanState.ImageQualityChecking.Bad)
            }
        }
        tracker.setImageQualityCheckingResult(false)
        job.cancel()
    }

    @Test
    fun `resetToIdle emits Idle state`() = runTest {
        val job = launch {
            tracker.state.collect { state ->
                assertThat(state).isEqualTo(FingerprintScanState.Idle)
            }
        }
        tracker.resetToIdle()
        job.cancel()
    }
}
