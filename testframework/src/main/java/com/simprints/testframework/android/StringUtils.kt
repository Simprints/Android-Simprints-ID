package com.simprints.testframework.android

import androidx.test.InstrumentationRegistry

object StringUtils {

    fun getResourceString(id: Int): String {
        val targetContext = InstrumentationRegistry.getTargetContext()
        return targetContext.resources.getString(id)
    }
}
