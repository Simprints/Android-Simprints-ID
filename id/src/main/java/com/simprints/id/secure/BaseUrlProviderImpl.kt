package com.simprints.id.secure

import com.google.firebase.FirebaseApp
import com.simprints.core.network.NetworkConstants.Companion.BASE_URL_SUFFIX
import com.simprints.core.network.NetworkConstants.Companion.DEFAULT_BASE_URL
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager

class BaseUrlProviderImpl(
    private val settingsPreferencesManager: SettingsPreferencesManager
) : BaseUrlProvider {

    override fun getApiBaseUrl(): String = settingsPreferencesManager.apiBaseUrl

    override fun setApiBaseUrl(apiBaseUrl: String?) {
        val prefix = "https://"
        val newValue = if (apiBaseUrl?.equals(DEFAULT_BASE_URL) == false) {
            if (apiBaseUrl.startsWith(prefix))
                "$apiBaseUrl$BASE_URL_SUFFIX"
            else
                "$prefix$apiBaseUrl$BASE_URL_SUFFIX"
        } else {
            DEFAULT_BASE_URL
        }

        settingsPreferencesManager.apiBaseUrl = newValue
    }

    override fun resetApiBaseUrl() {
        settingsPreferencesManager.apiBaseUrl = DEFAULT_BASE_URL
    }

    override fun getImageStorageBucketUrl(): String {
        val storedValue = settingsPreferencesManager.imageStorageBucketUrl

        return if (storedValue.isEmpty()) {
            val firebaseProjectName = FirebaseApp.getInstance().options.projectId
            "gs://$firebaseProjectName-images-eu"
        } else {
            storedValue
        }
    }

    override fun setImageStorageBucketUrl(imageStorageBucketUrl: String) {
        settingsPreferencesManager.imageStorageBucketUrl = imageStorageBucketUrl
    }

}
