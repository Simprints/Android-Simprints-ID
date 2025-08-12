package com.simprints.infra.logging.usecases

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit

class UpdateAndGetVersionHistoryUseCase {
    operator fun invoke(
        context: Context,
        currentVersion: String,
    ): String {
        // Keeping the preferences initialised only in the scope of the function call
        val preference = context.getSharedPreferences(VERSION_CACHE_NAME, MODE_PRIVATE)

        val versions = preference.getString(VERSIONS_KEY, null).orEmpty()
        if (versions.startsWith(currentVersion)) return versions

        val newVersions =
            if (versions.isEmpty()) currentVersion else "$currentVersion$VERSION_DELIMITER$versions"
        return newVersions
            .let {
                // Keep the total length within custom key constrains
                if (it.length > MAX_VALUE_LENGTH) it.substringBeforeLast(VERSION_DELIMITER) else it
            }.also { preference.edit { putString(VERSIONS_KEY, it) } }
    }

    companion object Companion {
        private const val VERSION_CACHE_NAME = "574b2793-7087-42ff-a80f-f9ad7cc3e54e"
        private const val VERSIONS_KEY = "versions"

        private const val VERSION_DELIMITER = ";"

        // This limit is based on the firebase/crashlytics message limit and
        // gives us ~6 version names or ~10 version codes of tracking
        private const val MAX_VALUE_LENGTH = 95
    }
}
