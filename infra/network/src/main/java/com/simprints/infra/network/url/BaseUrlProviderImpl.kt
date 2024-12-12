package com.simprints.infra.network.url

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class BaseUrlProviderImpl @Inject constructor(
    @ApplicationContext context: Context,
) : BaseUrlProvider {
    companion object {
        private const val PREF_FILE_NAME = "b3f0cf9b-4f3f-4c5b-bf85-7b1f44eddd7a"
        private const val PREF_MODE = Context.MODE_PRIVATE
        private const val API_BASE_URL_KEY = "ApiBaseUrl"
        private const val API_VERSION = "v2"
        private const val BASE_URL_SUFFIX = "/androidapi/$API_VERSION/"

        @VisibleForTesting
        const val DEFAULT_BASE_URL =
            "https://${BuildConfig.BASE_URL_PREFIX}.simprints-apis.com$BASE_URL_SUFFIX"
    }

    val prefs: SharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, PREF_MODE)

    override fun getApiBaseUrl(): String = prefs
        .getString(API_BASE_URL_KEY, DEFAULT_BASE_URL)!!
        .also { Simber.d("API base URL is $it") }

    override fun getApiBaseUrlPrefix(): String = prefs
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

        prefs.edit(commit = true) { putString(API_BASE_URL_KEY, newValue) }
    }

    override fun resetApiBaseUrl() {
        Simber.d("Resetting API base")
        prefs.edit(commit = true) { putString(API_BASE_URL_KEY, DEFAULT_BASE_URL) }
    }
}
