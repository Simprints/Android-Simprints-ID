package com.simprints.fingerprint.activities.collect.state

data class CollectFingerprintsState(
    var fingerStates: List<FingerState>,
    var currentFingerIndex: Int = 0,
    var isAskingRescan: Boolean = false,
    var isShowingConfirmDialog: Boolean = false,
    var isShowingSplashScreen: Boolean = false
) {

    fun currentFingerState(): FingerState = fingerStates[currentFingerIndex]
    fun currentCaptureState(): CaptureState = currentFingerState().currentCapture()
    fun isOnLastFinger(): Boolean = currentFingerIndex >= fingerStates.size - 1
}
