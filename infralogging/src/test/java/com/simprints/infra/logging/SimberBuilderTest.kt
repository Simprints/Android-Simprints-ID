package com.simprints.infra.logging

import com.simprints.infra.logging.LoggingTestUtils.setDebugBuildConfig
import io.mockk.mockk
import org.junit.Test
import timber.log.Timber

class SimberBuilderTest {

    @Test
    fun `initialize in debug mode should only create debug tree`() {
        setDebugBuildConfig(true)

        SimberBuilder.initialize(mockk(relaxed = true))

        assert(Timber.treeCount == 1)
    }

    @Test
    fun `initialize in release mode should create 2 trees`() {
        setDebugBuildConfig(false)

        try {
            SimberBuilder.initialize(mockk(relaxed = true))
        } catch (ex: Exception) {
            assert(ex is IllegalStateException)
        }
    }


}
