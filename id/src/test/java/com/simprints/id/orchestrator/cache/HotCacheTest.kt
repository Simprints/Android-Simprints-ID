package com.simprints.id.orchestrator.cache

import org.junit.Test

class HotCacheTest {

    private val hotCache = HotCache()

    @Test
    fun preferencesShouldNotBeNull() {
        val prefs = hotCache.preferences
        println("${prefs.hashCode()}")
    }

}
