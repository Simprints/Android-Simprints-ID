package com.simprints.fingerprint.capture.screen

import android.content.Context
import com.simprints.fingerprint.infra.scanner.ScannerManager
import com.simprints.fingerprint.infra.scanner.capture.FingerprintScanState
import com.simprints.fingerprint.infra.scanner.capture.FingerprintScanningStatusTracker
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.models.Vero2Configuration
import com.simprints.infra.config.store.models.Vero2Configuration.LedsMode.BASIC
import com.simprints.infra.config.store.models.Vero2Configuration.LedsMode.VISUAL_SCAN_FEEDBACK
import com.simprints.infra.config.sync.ConfigManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class ObserveFingerprintScanStatusUseCase @Inject constructor(
    private val statusTracker: FingerprintScanningStatusTracker,
    private val playAudioBeep: PlayAudioBeepUseCase,
    private val configManager: ConfigManager,
    private val scannerManager: ScannerManager,
    @ApplicationContext private val context: Context,
) {
    private var observeJob: Job? = null
    private var previousState: FingerprintScanState = FingerprintScanState.Idle
    private var ledsMode: Vero2Configuration.LedsMode? = BASIC
    private val preference = PreferenceManager.getDefaultSharedPreferences(context)

    operator fun invoke(
        coroutineScope: CoroutineScope, fingerprintSdk: FingerprintConfiguration.BioSdk
    ) {
        stopObserving()
        observeJob = coroutineScope.launch {
            ledsMode = configManager.getProjectConfiguration().fingerprint?.getSdkConfiguration(
                fingerprintSdk
            )?.vero2?.ledsMode
            statusTracker.state.collect { state ->
                provideFeedback(state)
                previousState = state
            }
        }
    }


    private suspend fun provideFeedback(state: FingerprintScanState) {
        when (state) {
            is FingerprintScanState.Idle -> turnOnFlashingWhiteSmileLeds()
            is FingerprintScanState.Scanning -> turnOffSmileLeds()
            is FingerprintScanState.ScanCompleted -> playRemoveFingerAudio()
            is FingerprintScanState.ImageQualityChecking.Good -> setUiAfterScan(true)
            is FingerprintScanState.ImageQualityChecking.Bad -> setUiAfterScan(false)
        }
    }

    fun stopObserving() {
        observeJob?.cancel()
        playAudioBeep.releaseMediaPlayer()
        observeJob = null
    }

    private suspend fun turnOnFlashingWhiteSmileLeds() {
        if (ledsMode == VISUAL_SCAN_FEEDBACK) {
            scannerManager.scanner.turnOnFlashingWhiteSmileLeds()
        }
    }

    private suspend fun turnOffSmileLeds() {
        if (ledsMode == VISUAL_SCAN_FEEDBACK) {
            scannerManager.scanner.turnOffSmileLeds()
        }
    }

    private suspend fun setUiAfterScan(isGoodScan: Boolean) {
        // Check if the previous state was ScanCompleted to avoid displaying the bad or good scan UI twice
        // There's no need to check the configuration, as the good/bad scan visual notifications apply across all LED modes.
        if (previousState == FingerprintScanState.ScanCompleted) {
            with(scannerManager.scanner) {
                if (isGoodScan) setUiGoodCapture()
                else setUiBadCapture()

                //Wait before turn of the leds
                delay(LONG_DELAY)
                turnOffSmileLeds()
            }
        }
    }

    private fun playRemoveFingerAudio() {
        // Verify that the previous state was not "ScanCompleted" to prevent the sound from playing twice.
        if (previousState == FingerprintScanState.ScanCompleted) return

        if (isAudioEnabled()) playAudioBeep()
    }

    private fun isAudioEnabled() = preference.getBoolean(AUDIO_PREFERENCE_KEY, false)

    companion object {
        const val LONG_DELAY = 3000L
        private const val AUDIO_PREFERENCE_KEY = "preference_enable_audio_on_scan_complete_key"
    }
}
