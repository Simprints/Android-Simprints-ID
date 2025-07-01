package com.simprints.infra.network.url

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.BuildConfig
import com.simprints.infra.security.SecurityManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class BaseUrlProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    securityManager: SecurityManager,
) : BaseUrlProvider {
    companion object {
        private const val LEGACY_PREF_FILE_NAME = "b3f0cf9b-4f3f-4c5b-bf85-7b1f44eddd7a"
        private const val SECURE_PREF_FILE_NAME = "97285f18-9742-469e-accf-3ed54def7a7e"
        private const val API_BASE_URL_KEY = "ApiBaseUrl"
        private const val API_VERSION = "v2"
        private const val BASE_URL_SUFFIX = "/androidapi/$API_VERSION/"

        @VisibleForTesting
        const val DEFAULT_BASE_URL =
            "https://${BuildConfig.BASE_URL_PREFIX}.simprints-apis.com$BASE_URL_SUFFIX"
    }

    private val securePrefs = securityManager.buildEncryptedSharedPreferences(SECURE_PREF_FILE_NAME)

    /**
     * Ensures that data has been migrated to secure prefs before accessing it.
     */
    private fun getSecurePrefs(): SharedPreferences {
        // Data has been migrated to secure prefs.
        // TODO Delete after there are no users below 2025.3.0
        if (!securePrefs.contains(API_BASE_URL_KEY)) {
            val prefs = context.getSharedPreferences(LEGACY_PREF_FILE_NAME, Context.MODE_PRIVATE)
            securePrefs.edit(commit = true) { putString(API_BASE_URL_KEY, prefs.getString(API_BASE_URL_KEY, "")) }
            prefs.edit(commit = true) { clear() }
        }
        return securePrefs
    }

    override fun getApiBaseUrl(): String = getSecurePrefs()
        .getString(API_BASE_URL_KEY, DEFAULT_BASE_URL)!!
        .also { Simber.d("API base URL is $it") }

    override fun getApiBaseUrlPrefix(): String = getSecurePrefs()
        .getString(API_BASE_URL_KEY, DEFAULT_BASE_URL)
        ?.removeSuffix(BASE_URL_SUFFIX)
        ?.also { Simber.d("API base URL prefix is $it") }!!

    override fun setApiBaseUrl(apiBaseUrl: String?) {
        val prefix = "https://"
        val newValue = if (apiBaseUrl?.equals(DEFAULT_BASE_URL) == false) {
            if (apiBaseUrl.startsWith(prefix)) {
                "$apiBaseUrl$BASE_URL_SUFFIX"
            } else {
                "$prefix$apiBaseUrl$BASE_URL_SUFFIX"
            }
        } else {
            DEFAULT_BASE_URL
        }

        Simber.d("Setting API base URL to $newValue")

        getSecurePrefs().edit(commit = true) { putString(API_BASE_URL_KEY, newValue) }
    }

    override fun resetApiBaseUrl() {
        Simber.d("Resetting API base")
        getSecurePrefs().edit(commit = true) { putString(API_BASE_URL_KEY, DEFAULT_BASE_URL) }
    }
}
