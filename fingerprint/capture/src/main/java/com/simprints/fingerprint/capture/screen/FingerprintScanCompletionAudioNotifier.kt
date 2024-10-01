package com.simprints.fingerprint.capture.screen

import android.content.Context
import android.media.MediaPlayer
import androidx.preference.PreferenceManager
import com.simprints.fingerprint.capture.R
import com.simprints.fingerprint.infra.scanner.capture.FingerprintScanningStatusTracker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class FingerprintScanCompletionAudioNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scanningStatusTracker: FingerprintScanningStatusTracker,
) {
    private var mediaPlayer: MediaPlayer? = null

    suspend fun observeScanStatus() {
        scanningStatusTracker.scanCompleted.collect {
            if (isAudioEnabled()) playBeep()
        }
    }

    private fun playBeep() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.beep)
        }
        mediaPlayer?.start()
    }

    private fun isAudioEnabled(): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(AUDIO_PREFERENCE_KEY, true)
    }

    fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    companion object {
        private const val AUDIO_PREFERENCE_KEY = "preference_enable_audio_on_scan_complete_key"
    }
}
