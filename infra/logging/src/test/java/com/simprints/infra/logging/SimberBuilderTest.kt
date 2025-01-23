package com.simprints.infra.logging

import co.touchlab.kermit.Logger
import io.mockk.mockk
import org.junit.Test

class SimberBuilderTest {
    @Test
    fun `initialize in debug mode should only create debug tree`() {
        SimberBuilder.initialize(mockk(relaxed = true))
        assert(Logger.config.logWriterList.count() == 1)
    }
}
