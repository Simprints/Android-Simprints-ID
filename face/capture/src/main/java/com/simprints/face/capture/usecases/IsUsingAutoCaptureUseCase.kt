package com.simprints.face.capture.usecases

import android.content.Context
import androidx.preference.PreferenceManager
import com.simprints.infra.config.store.models.experimental
import com.simprints.infra.config.sync.ConfigManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IsUsingAutoCaptureUseCase @Inject constructor(
    private val configManager: ConfigManager,
    @ApplicationContext private val context: Context,
) {
    private val preference = PreferenceManager.getDefaultSharedPreferences(context)

    suspend operator fun invoke(): Boolean {
        val isFeatureEnabled = configManager.getProjectConfiguration().experimental().faceAutoCaptureEnabled
        val isOptionTurnedOnInSettings = preference.getBoolean(AUTO_CAPTURE_PREFERENCE_KEY, true)
        return isFeatureEnabled && isOptionTurnedOnInSettings
    }

    companion object {
        private const val AUTO_CAPTURE_PREFERENCE_KEY = "preference_enable_face_auto_capture"
    }
}
