package com.simprints.id.secure

import com.simprints.core.network.NetworkConstants.Companion.BASE_URL_SUFFIX
import com.simprints.core.network.NetworkConstants.Companion.DEFAULT_BASE_URL
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager

class BaseUrlProviderImpl(
    private val settingsPreferencesManager: SettingsPreferencesManager
) : BaseUrlProvider {

    override fun getApiBaseUrl(): String = settingsPreferencesManager.apiBaseUrl

    override fun setApiBaseUrl(apiBaseUrl: String?) {
        val newValue = if (apiBaseUrl?.equals(DEFAULT_BASE_URL) == false)
            "https://$apiBaseUrl$BASE_URL_SUFFIX"
        else
            DEFAULT_BASE_URL

        settingsPreferencesManager.apiBaseUrl = newValue
    }

}
