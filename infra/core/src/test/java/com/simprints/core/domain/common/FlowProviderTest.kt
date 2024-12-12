package com.simprints.core.domain.common

import org.junit.Test

class FlowProviderTest {
    @Test
    fun `enrol value returns current type`() {
        assert(FlowType.valueOf("ENROL") == FlowType.ENROL)
    }

    @Test
    fun `identify value returns current type`() {
        assert(FlowType.valueOf("IDENTIFY") == FlowType.IDENTIFY)
    }

    @Test
    fun `verify value returns current type`() {
        assert(FlowType.valueOf("VERIFY") == FlowType.VERIFY)
    }
}
