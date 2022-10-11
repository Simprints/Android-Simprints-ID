package com.simprints.core.domain.common

import org.junit.Test

class FlowProviderTest {


    @Test
    fun `enrol value returns current type`() {
        assert(FlowProvider.FlowType.valueOf("ENROL") == FlowProvider.FlowType.ENROL)
    }

    @Test
    fun `identify value returns current type`() {
        assert(FlowProvider.FlowType.valueOf("IDENTIFY") == FlowProvider.FlowType.IDENTIFY)
    }

    @Test
    fun `verify value returns current type`() {
        assert(FlowProvider.FlowType.valueOf("VERIFY") == FlowProvider.FlowType.VERIFY)
    }

}
