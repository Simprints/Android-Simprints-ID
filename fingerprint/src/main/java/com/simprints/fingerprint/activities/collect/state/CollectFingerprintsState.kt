package com.simprints.fingerprint.activities.collect.state

import com.simprints.fingerprint.activities.collect.domain.Finger

data class CollectFingerprintsState(
    val fingerStates: Map<Finger, FingerCollectionState>,
    var currentFingerIndex: Int = 0
) {

    fun orderedFingers(): List<Finger> = fingerStates.keys.sorted()
    fun currentFinger(): Finger = orderedFingers()[currentFingerIndex]
    fun currentFingerState(): FingerCollectionState = fingerStates.getValue(currentFinger())
    fun isOnLastFinger(): Boolean = currentFingerIndex >= fingerStates.size - 1
}
