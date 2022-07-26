package com.simprints.infra.network.exceptions

import okhttp3.internal.http2.ConnectionShutdownException
import java.io.IOException

class NetworkConnectionException(message: String = "NetworkConnectionException", cause: Throwable) :
    RuntimeException(message, cause)

fun Throwable.isNetworkConnectionException(): Boolean {
    return when {
        isNetworkConnectionException(this) -> true
        // Check the cause in case exception was rethrown
        isNetworkConnectionException(this.cause) -> true
        else -> false
    }
}

fun Throwable.isNetworkConnectionException(throwable: Throwable?): Boolean {
    return when (throwable) {
        is IOException, is ConnectionShutdownException -> true
        else -> false
    }
}
