package com.simprints.core.network

import com.test.core.BuildConfig

class NetworkConstants {
    companion object {
        private const val apiVersion = "v1"
        const val baseUrl = "https://${BuildConfig.END_POINT}.simprints-apis.com/androidapi/$apiVersion/"
    }
}
