package com.simprints.fingerprint.capture.usecase

import com.simprints.fingerprint.capture.state.CaptureState
import com.simprints.infra.config.store.models.FingerprintConfiguration
import javax.inject.Inject

internal class IsNoFingerDetectedLimitReachedUseCase @Inject constructor() {
    operator fun invoke(
        fingerState: CaptureState,
        sdkConfiguration: FingerprintConfiguration.FingerprintSdkConfiguration
    ): Boolean = when (fingerState) {
        is CaptureState.ScanProcess -> {
            val noFingerDetectedThreshold =
                sdkConfiguration.maxCaptureAttempts?.noFingerDetected?.takeIf { it > 1 }
                    ?: MAXIMUM_LIMIT_OF_NO_FINGER_DETECTED_SCANS
            fingerState.numberOfNoFingerDetectedScans >= noFingerDetectedThreshold
        }

        else -> false
    }

    companion object {
        const val MAXIMUM_LIMIT_OF_NO_FINGER_DETECTED_SCANS = 40  // current maximum value in Vulcan
    }
}
