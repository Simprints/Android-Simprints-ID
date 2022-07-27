package com.simprints.infra.network.url

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import com.simprints.infra.network.BuildConfig

class BaseUrlProviderImpl(context: Context) : BaseUrlProvider {

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

    override fun getApiBaseUrl(): String = prefs.getString(API_BASE_URL_KEY, DEFAULT_BASE_URL)!!

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

        prefs.edit().putString(API_BASE_URL_KEY, newValue).apply()
    }

    override fun resetApiBaseUrl() =
        prefs.edit().putString(API_BASE_URL_KEY, DEFAULT_BASE_URL).apply()


}
