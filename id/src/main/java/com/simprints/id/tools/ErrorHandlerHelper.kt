package com.simprints.id.tools

suspend fun <T> ignoreException(block: suspend () -> T): T? =
    try {
        block()
    } catch (t: Throwable) {
        null
    }
