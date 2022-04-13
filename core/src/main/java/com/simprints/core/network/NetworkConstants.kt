package com.simprints.core.network

import com.simprints.core.BuildConfig

class NetworkConstants {
    companion object {
        private const val API_VERSION = "v2"
        const val BASE_URL_SUFFIX = "/androidapi/$API_VERSION/"
        const val DEFAULT_BASE_URL = "https://${BuildConfig.BASE_URL_PREFIX}.simprints-apis.com$BASE_URL_SUFFIX"
        val httpCodesForRecoverableCloudIssues = listOf(500, 502, 503)
        const val RETRY_ATTEMPTS_FOR_NETWORK_CALLS = 5
        const val AUTHORIZATION_ERROR = 403
        const val BACKEND_MAINTENANCE_ERROR_STRING = "{\"error\":\"002\"}"
        const val HEADER_RETRY_AFTER = "retry-after"
    }
}
