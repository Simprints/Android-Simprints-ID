package com.simprints.core.tools.exceptions

import com.simprints.infra.logging.Simber

suspend fun <T> ignoreException(block: suspend () -> T): T? = try {
    block()
} catch (t: Throwable) {
    Simber.i("Ignored exception", t)
    null
}
