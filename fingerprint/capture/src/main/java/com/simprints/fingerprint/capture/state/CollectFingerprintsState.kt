package com.simprints.fingerprint.capture.state

internal data class CollectFingerprintsState(
    val fingerStates: List<FingerState>,
    val currentFingerIndex: Int,
    val isAskingRescan: Boolean,
    val confirmationDialogState: ConfirmationDialogState,
    val isShowingSplashScreen: Boolean,
    val isShowingConnectionScreen: Boolean,
) {

    fun currentFingerState(): FingerState = fingerStates[currentFingerIndex]
    fun currentCaptureState(): CaptureState = currentFingerState().currentCapture()
    fun isOnLastFinger(): Boolean = currentFingerIndex >= fingerStates.size - 1
    fun isShowingDialog(): Boolean = confirmationDialogState is ConfirmationDialogState.Displayed

    companion object {
        val EMPTY = CollectFingerprintsState(
            fingerStates = emptyList(),
            currentFingerIndex = 0,
            isAskingRescan = false,
            confirmationDialogState = ConfirmationDialogState.NotDisplayed,
            isShowingSplashScreen = false,
            isShowingConnectionScreen = false,
        )
    }
}

sealed class ConfirmationDialogState {
    data object NotDisplayed : ConfirmationDialogState()
    data class Displayed(
        val isSuccessful: Boolean,
        val lowerThreshold: Int?,
        val upperThreshold: Int?,
        val successfulCaptures: Int
    ) : ConfirmationDialogState()
}