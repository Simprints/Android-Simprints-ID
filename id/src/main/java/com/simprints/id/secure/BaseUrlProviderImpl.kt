package com.simprints.id.secure

import com.simprints.id.network.BaseUrlProvider
import com.simprints.id.network.NetworkConstants.Companion.BASE_URL_SUFFIX
import com.simprints.id.network.NetworkConstants.Companion.DEFAULT_BASE_URL
import com.simprints.id.data.db.project.local.ProjectLocalDataSource
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager

class BaseUrlProviderImpl(
    private val settingsPreferencesManager: SettingsPreferencesManager,
    private val projectLocalDataSource: ProjectLocalDataSource,
    private val loginInfoManager: LoginInfoManager
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

    override suspend fun getImageStorageBucketUrl(): String? {
        val projectId = loginInfoManager.getSignedInProjectIdOrEmpty()

        if (projectId.isEmpty())
            return null

        val imageStorageBucketUrl = projectLocalDataSource.load(projectId)?.imageBucket

        return if (imageStorageBucketUrl.isNullOrEmpty()) {
            "gs://$projectId-images-eu"
        } else {
            imageStorageBucketUrl
        }
    }

}
