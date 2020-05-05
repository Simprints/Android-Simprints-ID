package com.simprints.fingerprint.activities.collect.state

sealed class FingerCollectionState {
    object NotCollected : FingerCollectionState()
    object Skipped : FingerCollectionState()
    object Scanning: FingerCollectionState()
    object TransferringImage : FingerCollectionState()
    object NotDetected : FingerCollectionState()
    class Collected(val fingerScanResult: FingerScanResult, var numberOfBadScans: Int = 0): FingerCollectionState()

    fun isBusy(): Boolean = this == Scanning || this == TransferringImage
}
