package com.simprints.fingerprint.infra.scanner.capture

sealed class FingerprintScanState {
    data object Idle : FingerprintScanState()

    data object Scanning : FingerprintScanState()

    data object ScanCompleted : FingerprintScanState()

    sealed class ImageQualityChecking : FingerprintScanState() {
        data object Good : ImageQualityChecking()

        data object Bad : ImageQualityChecking()
    }
}
