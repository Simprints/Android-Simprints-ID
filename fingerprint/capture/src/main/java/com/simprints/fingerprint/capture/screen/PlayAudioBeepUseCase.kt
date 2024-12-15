package com.simprints.fingerprint.capture.screen

import android.content.Context
import android.media.MediaPlayer
import com.simprints.fingerprint.capture.R
import com.simprints.infra.logging.Simber
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PlayAudioBeepUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var mediaPlayer: MediaPlayer? = null

    operator fun invoke() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.beep)
        }
        Simber.d("Playing beep sound")
        mediaPlayer?.start()
    }

    fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
