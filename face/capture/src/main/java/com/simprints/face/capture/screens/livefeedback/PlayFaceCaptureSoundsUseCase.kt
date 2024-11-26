package com.simprints.face.capture.screens.livefeedback

import android.content.Context
import android.media.MediaPlayer
import android.provider.Settings
import com.simprints.face.capture.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PlayFaceCaptureSoundsUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var mediaPlayer: MediaPlayer? = null

    fun playAttentionSound() {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, R.raw.camera_shutter_multiple)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }

    fun playCameraShutterSound() {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, R.raw.camera_shutter_single)
        mediaPlayer?.isLooping = false
        mediaPlayer?.start()
    }

    fun stopSound() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}