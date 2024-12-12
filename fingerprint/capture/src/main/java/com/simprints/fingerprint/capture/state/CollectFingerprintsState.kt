package com.simprints.fingerprint.capture.state

internal data class CollectFingerprintsState(
    val fingerStates: List<FingerState>,
    val currentFingerIndex: Int,
    val isAskingRescan: Boolean,
    val isShowingConfirmDialog: Boolean,
    val isShowingSplashScreen: Boolean,
    val isShowingConnectionScreen: Boolean,
) {
    fun currentFingerState(): FingerState = fingerStates[currentFingerIndex]

    fun currentCaptureState(): CaptureState = currentFingerState().currentCapture()

    fun isOnLastFinger(): Boolean = currentFingerIndex >= fingerStates.size - 1

    companion object {
        val EMPTY = CollectFingerprintsState(
            fingerStates = emptyList(),
            currentFingerIndex = 0,
            isAskingRescan = false,
            isShowingConfirmDialog = false,
            isShowingSplashScreen = false,
            isShowingConnectionScreen = false,
        )
    }
}
