package com.simprints.fingerprint.activities.collect.state

sealed class FingerCollectionState {
    object NotCollected : FingerCollectionState()
    object Skipped : FingerCollectionState()
    object Collected: FingerCollectionState()
}
