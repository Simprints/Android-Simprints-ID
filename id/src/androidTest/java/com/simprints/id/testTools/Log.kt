package com.simprints.id.testTools

import android.app.Instrumentation
import android.os.Bundle
import android.support.test.InstrumentationRegistry
import timber.log.Timber

fun log(message: String) {
    Timber.d(String.format("EndToEndTesting: %s", message))
    val b = Bundle()
    b.putString(Instrumentation.REPORT_KEY_STREAMRESULT, "\n" + message)
    InstrumentationRegistry.getInstrumentation().sendStatus(0, b)
}
