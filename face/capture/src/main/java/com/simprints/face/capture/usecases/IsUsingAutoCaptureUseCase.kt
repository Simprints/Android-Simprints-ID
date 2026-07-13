package com.simprints.face.capture.usecases

import android.content.Context
import androidx.preference.PreferenceManager
import com.simprints.infra.config.store.models.ProjectConfiguration
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IsUsingAutoCaptureUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val preference = PreferenceManager.getDefaultSharedPreferences(context)

    operator fun invoke(projectConfiguration: ProjectConfiguration): Boolean {
        val isFeatureEnabled = projectConfiguration.face?.isAutoCapture ?: false
        return isFeatureEnabled && isOptionTurnedOnInSettings()
    }

    private fun isOptionTurnedOnInSettings(): Boolean = preference.getBoolean(AUTO_CAPTURE_PREFERENCE_KEY, true)

    companion object {
        private const val AUTO_CAPTURE_PREFERENCE_KEY = "preference_enable_face_auto_capture"
    }
}
