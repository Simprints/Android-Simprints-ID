package com.simprints.id.tools

import android.support.test.InstrumentationRegistry


object StringUtils {

    fun getResourceString(id: Int): String {
        val targetContext = InstrumentationRegistry.getTargetContext()
        return targetContext.resources.getString(id)
    }
}
