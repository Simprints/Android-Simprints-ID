package com.simprints.id.network

import com.test.core.BuildConfig

class NetworkConstants {
    companion object {
        private const val API_VERSION = "v2"
        const val BASE_URL_SUFFIX = "/androidapi/$API_VERSION/"
        const val DEFAULT_BASE_URL = "https://${BuildConfig.BASE_URL_PREFIX}.simprints-apis.com$BASE_URL_SUFFIX"
        val httpCodesForIntegrationIssues = (400..404) + (500..599) - (502..503)
        const val RETRY_ATTEMPTS_FOR_NETWORK_CALLS = 5
    }
}
