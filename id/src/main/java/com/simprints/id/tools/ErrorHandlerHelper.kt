package com.simprints.id.tools

import com.simprints.logging.Simber

suspend fun <T> ignoreException(block: suspend () -> T): T? =
    try {
        block()
    } catch (t: Throwable) {
        Simber.d(t)
        null
    }
