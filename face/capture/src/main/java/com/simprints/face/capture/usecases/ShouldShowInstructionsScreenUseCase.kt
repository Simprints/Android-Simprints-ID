package com.simprints.face.capture.usecases

import androidx.annotation.VisibleForTesting
import com.simprints.infra.security.SecurityManager
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class ShouldShowInstructionsScreenUseCase @Inject constructor(
    private val securityManager: SecurityManager,
) {
    operator fun invoke(): Boolean {
        val sharedPrefs = securityManager.buildEncryptedSharedPreferences(FILENAME_FOR_INSTRUCTIONS_SHOWING_SHARED_PREFS)
        val areInstructionsShowing = sharedPrefs.getBoolean(INSTRUCTIONS_SHOWING_PREFERENCE_KEY, true)
        if (areInstructionsShowing) {
            sharedPrefs.edit {
                putBoolean(INSTRUCTIONS_SHOWING_PREFERENCE_KEY, false)
            }
        }
        return areInstructionsShowing
    }

    companion object {
        private const val FILENAME_FOR_INSTRUCTIONS_SHOWING_SHARED_PREFS = "INSTRUCTIONS_SHOWING"
        @VisibleForTesting
        const val INSTRUCTIONS_SHOWING_PREFERENCE_KEY = "preference_instructions_showing"
    }
}
