package com.simprints.infra.logging

import org.robolectric.util.ReflectionHelpers

internal object LoggingTestUtils {

    fun setDebugBuildConfig(isDebug: Boolean) {
        if (isDebug)
            ReflectionHelpers.setStaticField(BuildConfig::class.java, "DEBUG", true)
        else
            ReflectionHelpers.setStaticField(BuildConfig::class.java, "DEBUG", false)
    }

}
