package com.simprints.infra.logging

import org.junit.Test

class PerformanceMonitorTest {
    @Test
    fun `initialize in debug mode should not create performance monitor`() {
        val trace = PerformanceMonitor.getTrace("Test Name", Simber)

        assert(trace.newTrace == null)
    }
}
