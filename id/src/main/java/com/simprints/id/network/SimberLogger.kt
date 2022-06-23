package com.simprints.id.network

import com.simprints.infra.logging.Simber
import okhttp3.logging.HttpLoggingInterceptor

object SimberLogger: HttpLoggingInterceptor.Logger {

    override fun log(message: String) {
        Simber.d(message)
    }
}
