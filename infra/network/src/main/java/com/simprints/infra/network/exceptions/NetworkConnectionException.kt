package com.simprints.infra.network.exceptions

import okhttp3.internal.http2.ConnectionShutdownException
import java.io.IOException

class NetworkConnectionException(
    message: String = "NetworkConnectionException",
    cause: Throwable,
) : RuntimeException(message, cause)

fun Throwable.isCausedFromBadNetworkConnection(): Boolean = when {
    isCausedFromBadNetworkConnection(this) -> true
    // Check the cause in case exception was rethrown
    isCausedFromBadNetworkConnection(this.cause) -> true
    else -> false
}

fun Throwable.isCausedFromBadNetworkConnection(throwable: Throwable?): Boolean = when (throwable) {
    is IOException, is ConnectionShutdownException -> true
    else -> false
}
