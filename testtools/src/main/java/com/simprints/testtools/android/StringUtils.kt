package com.simprints.testtools.android

import androidx.test.platform.app.InstrumentationRegistry

fun getResourceString(id: Int): String {
    val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
    return targetContext.resources.getString(id)
}
