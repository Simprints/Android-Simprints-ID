package com.simprints.id.tools

import android.app.Instrumentation
import android.os.Bundle
import android.support.test.InstrumentationRegistry
import android.util.Log


fun log(message: String) {
    Log.d("EndToEndTesting", message)
    val b = Bundle()
    b.putString(Instrumentation.REPORT_KEY_STREAMRESULT, "\n" + message)
    InstrumentationRegistry.getInstrumentation().sendStatus(0, b)
}
