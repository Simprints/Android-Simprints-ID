package com.simprints.fingerprint.infra.scanner.capture

import com.google.common.truth.Truth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FingerprintScanningStatusTrackerTest {
    private val tracker = FingerprintScanningStatusTracker()
    private val testDispatcher = UnconfinedTestDispatcher()

    @Test
    fun `test notifyScanCompleted emits Unit`() = runTest(testDispatcher) {
        var emitted = false
        val job = launch {
            tracker.scanCompleted.collect {
                emitted = true
            }
        }
        tracker.notifyScanCompleted()
        Truth.assertThat(emitted).isTrue()
        job.cancel()
    }

    @Test
    fun `test scanCompleted flow does not replay past emissions`() = runTest(testDispatcher) {
        tracker.notifyScanCompleted()

        var emitted = false
        val job = launch {
            tracker.scanCompleted.collect {
                emitted = true
            }
        }
        Truth.assertThat(emitted).isFalse()
        job.cancel()
    }

}
