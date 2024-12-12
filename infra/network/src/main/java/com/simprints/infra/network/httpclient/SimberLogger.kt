package com.simprints.infra.network.httpclient

import com.simprints.infra.logging.Simber
import okhttp3.logging.HttpLoggingInterceptor

internal object SimberLogger : HttpLoggingInterceptor.Logger {
    override fun log(message: String) {
        Simber.d(message)
    }
}
