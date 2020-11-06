package com.simprints.id.tools

import timber.log.Timber

suspend fun <T> ignoreException(block: suspend () -> T): T? =
    try {
        block()
    } catch (t: Throwable) {
        Timber.d(t)
        null
    }
