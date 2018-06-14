package com.simprints.id.network

import com.simprints.id.BuildConfig

class NetworkConstants {
    companion object {
        private const val apiVersion = "v1"
        const val baseUrl = "https://${BuildConfig.END_POINT}.simprints-apis.com/androidapi/$apiVersion/"
    }
}
