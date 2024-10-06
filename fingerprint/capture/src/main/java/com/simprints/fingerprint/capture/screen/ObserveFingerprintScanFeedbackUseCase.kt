package com.simprints.fingerprint.capture.screen

import com.simprints.fingerprint.infra.scanner.ScannerManager
import com.simprints.fingerprint.infra.scanner.capture.FingerprintScanState
import com.simprints.fingerprint.infra.scanner.capture.FingerprintScanningStatusTracker
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObserveFingerprintScanFeedbackUseCase @Inject constructor(
    private val statusTracker: FingerprintScanningStatusTracker,
    private val playAudioBeep: PlayAudioBeepUseCase,
    private val configManager: ConfigManager,
    private val scannerManager: ScannerManager,
) {
    private var observeJob: Job? = null
    private var previousState: FingerprintScanState = FingerprintScanState.Idle

    operator fun invoke(coroutineScope: CoroutineScope) {
        stopObserving()
        observeJob = coroutineScope.launch {
            statusTracker.state.collect { state ->
                provideFeedback(state)
                previousState = state
            }
        }
    }


    private suspend fun provideFeedback(state: FingerprintScanState) {
        Simber.i("provideFeedback: $state")
        when (state) {
            is FingerprintScanState.Idle -> turnFlashingLedsOn()
            is FingerprintScanState.Scanning -> turnFlashingLedsOn()
            is FingerprintScanState.ScanCompleted -> setUiToRemoveFinger()
            is FingerprintScanState.ImageQualityChecking.Good -> setUiAfterScan(true)
            is FingerprintScanState.ImageQualityChecking.Bad -> setUiAfterScan(false)

        }
    }
    fun stopObserving() {
        observeJob?.cancel()
        playAudioBeep.releaseMediaPlayer()
        observeJob = null
    }

    private suspend fun turnFlashingLedsOn() {
        scannerManager.scanner.turnFlashingOrangeLeds()
    }

    private suspend fun setUiAfterScan(isGoodScan: Boolean) {
        // Check if the previous state was removeFinger to avoid displaying the bad or good scan UI twice
        if (previousState == FingerprintScanState.ScanCompleted) {
            with(scannerManager.scanner) {
                if (isGoodScan)
                    setUiGoodCapture()
                else
                    setUiBadCapture()

                //Wait before turn of the leds
                longDelay()
                turnOffSmileLeds()
            }
        }
    }

    private suspend fun setUiToRemoveFinger() {
        // Check if the previous state was not remove finger to avoid playing the sound twice
        if (previousState != FingerprintScanState.ScanCompleted) {
            playAudioBeep()
            scannerManager.scanner.turnOffSmileLeds()
        }
    }

    private suspend fun longDelay() = delay(LONG_DELAY)

    companion object {
        const val LONG_DELAY = 3000L
    }
}
