package com.simprints.id.network

import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber


class TimberLogger: HttpLoggingInterceptor.Logger {
    override fun log(message: String?) {
        message?.let {
            Timber.d(it)
        }
    }
}
