package com.simprints.fingerprint.activities.collect.state

data class CollectFingerprintsState(
    var fingerStates: List<FingerCollectionState>,
    var currentFingerIndex: Int = 0,
    var isAskingRescan: Boolean = false,
    var isShowingConfirmDialog: Boolean = false,
    var isShowingSplashScreen: Boolean = false
) {

    fun currentFingerState(): FingerCollectionState = fingerStates[currentFingerIndex]
    fun isOnLastFinger(): Boolean = currentFingerIndex >= fingerStates.size - 1
}
