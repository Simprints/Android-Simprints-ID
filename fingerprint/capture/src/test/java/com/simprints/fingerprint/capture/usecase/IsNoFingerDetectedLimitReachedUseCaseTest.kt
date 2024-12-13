package com.simprints.fingerprint.capture.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.capture.state.CaptureState
import com.simprints.infra.config.store.models.FingerprintConfiguration.FingerprintSdkConfiguration
import com.simprints.infra.config.store.models.MaxCaptureAttempts
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

internal class IsNoFingerDetectedLimitReachedUseCaseTest {
    private val isNoFingerDetectedLimitReachedUseCase = IsNoFingerDetectedLimitReachedUseCase()

    @Test
    fun `when capture state is not ScanProcess, then returns false`() {
        val fingerStateNotCollected = mockk<CaptureState.NotCollected>()
        val fingerStateSkipped = mockk<CaptureState.Skipped>()
        val sdkConfiguration = mockk<FingerprintSdkConfiguration>()

        listOf(fingerStateNotCollected, fingerStateSkipped).forEach { fingerState ->
            assertThat(
                isNoFingerDetectedLimitReachedUseCase(
                    fingerState = fingerState,
                    sdkConfiguration = sdkConfiguration,
                ),
            ).isFalse()
        }
    }

    @Test
    fun `when max capture attempts is null, then inclusive MAXIMUM_LIMIT_OF_NO_FINGER_DETECTED_SCANS is used`() {
        val fingerState = mockk<CaptureState.ScanProcess> {
            every { numberOfNoFingerDetectedScans } returns IsNoFingerDetectedLimitReachedUseCase.MAXIMUM_LIMIT_OF_NO_FINGER_DETECTED_SCANS
        }
        val sdkConfiguration = mockk<FingerprintSdkConfiguration> {
            every { maxCaptureAttempts } returns null
        }
        assertThat(
            isNoFingerDetectedLimitReachedUseCase(
                fingerState = fingerState,
                sdkConfiguration = sdkConfiguration,
            ),
        ).isTrue()
    }

    @Test
    fun `when number of no finger detected scans is greater than no finger detected threshold, then true is returned`() {
        val noFingerDetectedThreshold = 3
        val noFingerDetectedScans = noFingerDetectedThreshold
        val fingerState = mockk<CaptureState.ScanProcess> {
            every { numberOfNoFingerDetectedScans } returns noFingerDetectedScans
        }
        val sdkConfiguration = mockk<FingerprintSdkConfiguration> {
            every { maxCaptureAttempts } returns mockk<MaxCaptureAttempts> {
                every { noFingerDetected } returns noFingerDetectedThreshold
            }
        }
        assertThat(
            isNoFingerDetectedLimitReachedUseCase(
                fingerState = fingerState,
                sdkConfiguration = sdkConfiguration,
            ),
        ).isTrue()
    }

    @Test
    fun `when threshold number of no finger detected scans is lower than 2, then MAXIMUM_LIMIT_OF_NO_FINGER_DETECTED_SCANS is used`() {
        val noFingerDetectedThreshold =
            IsNoFingerDetectedLimitReachedUseCase.MAXIMUM_LIMIT_OF_NO_FINGER_DETECTED_SCANS
        val noFingerDetectedScans = noFingerDetectedThreshold - 1
        val fingerState = mockk<CaptureState.ScanProcess> {
            every { numberOfNoFingerDetectedScans } returns noFingerDetectedScans
        }
        val sdkConfiguration = mockk<FingerprintSdkConfiguration> {
            every { maxCaptureAttempts } returns mockk<MaxCaptureAttempts> {
                every { noFingerDetected } returns 1
            }
        }
        assertThat(
            isNoFingerDetectedLimitReachedUseCase(
                fingerState = fingerState,
                sdkConfiguration = sdkConfiguration,
            ),
        ).isFalse()
    }
}
