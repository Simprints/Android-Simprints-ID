package com.simprints.id.tools

import timber.log.Timber
import java.util.*

object Log {

    fun d(o: Any, s: String) {
        Timber.d(String.format(Locale.UK, "%s: %s", o.javaClass.simpleName, s))
    }
}
