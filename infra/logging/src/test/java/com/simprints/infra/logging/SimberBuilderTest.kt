package com.simprints.infra.logging

import io.mockk.mockk
import org.junit.Test
import timber.log.Timber

class SimberBuilderTest {
    @Test
    fun `initialize in debug mode should only create debug tree`() {
        SimberBuilder.initialize(mockk(relaxed = true))
        assert(Timber.treeCount == 1)
    }
}
