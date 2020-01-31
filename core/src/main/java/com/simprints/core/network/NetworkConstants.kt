package com.simprints.core.network

import com.test.core.BuildConfig

class NetworkConstants {
    companion object {
        private const val apiVersion = "v2"
        const val baseUrl = "https://${BuildConfig.END_POINT}.simprints-apis.com/androidapi/$apiVersion/"
        val httpCodesForIntegrationIssues = (400..404) + (500..599)
        const val RETRY_ATTEMPTS_FOR_NETWORK_CALLS = 5
    }
}
