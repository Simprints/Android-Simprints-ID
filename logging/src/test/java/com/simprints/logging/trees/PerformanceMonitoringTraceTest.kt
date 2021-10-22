package com.simprints.logging.trees

import com.google.firebase.perf.FirebasePerformance
import com.simprints.logging.LoggingTestUtils.setDebugBuildConfig
import com.simprints.logging.Simber
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test

class PerformanceMonitoringTraceTest {

    @Test
    fun `calling trace should try to create a new FPM trace`() {
        val simber = Simber
        val fpmMock: FirebasePerformance = mockk(relaxed = true)

        PerformanceMonitoringTrace(TRACE_NAME, fpmMock, simber)

        verify { fpmMock.newTrace(TRACE_NAME) }
    }

    @Test
    fun `if DEBUG is true FPM trace should not be created`() {
        val fpmMock: FirebasePerformance = mockk(relaxed = true)

        Simber.trace(TRACE_NAME)

        verify(exactly = 0) { fpmMock.newTrace(TRACE_NAME) }
    }

    @Test
    fun `if FPM is set trace start should start FPM trace`() {
        val fpmMock: FirebasePerformance = mockk(relaxed = true)
        val trace = spyk(PerformanceMonitoringTrace(TRACE_NAME, fpmMock, Simber))

        trace.start()

        verify { fpmMock.newTrace(TRACE_NAME).start() }
    }

    @Test
    fun `if FPM is set trace stop should stop FPM trace`() {
        val fpmMock: FirebasePerformance = mockk(relaxed = true)
        val trace = spyk(PerformanceMonitoringTrace(TRACE_NAME, fpmMock, Simber))

        trace.stop()

        verify { fpmMock.newTrace(TRACE_NAME).stop() }
    }

    @Test
    fun `if FPM isn't set trace start should not start FPM trace`() {
        val trace = spyk(PerformanceMonitoringTrace(TRACE_NAME, null, Simber))

        trace.start()

        assert(trace.newTrace == null)
        assert(trace.newTrace?.start() == null)
    }

    @Test
    fun `if FPM isn't set trace stop should not stop FPM trace`() {
        val trace = spyk(PerformanceMonitoringTrace(TRACE_NAME, null, Simber))

        trace.stop()

        assert(trace.newTrace == null)
        assert(trace.newTrace?.stop() == null)
    }

    @Test
    fun `if debug mode stopping trace should print time`() {
        setDebugBuildConfig(true)

        val simberSpy = spyk<Simber>()

        val trace = simberSpy.trace(TRACE_NAME)
        trace.start()
        trace.stop()

        verify {
            simberSpy.i(message = withArg {
                it.contains("Trace time for $TRACE_NAME =")
            }, null)
        }
    }

    @Test
    fun `if not debug mode stopping trace should not print time`() {
        setDebugBuildConfig(false)
        val simberSpy = spyk<Simber>()
        val trace = spyk(PerformanceMonitoringTrace(TRACE_NAME, mockk(relaxed = true), simberSpy))

        trace.start()
        trace.stop()

        verify(exactly = 0) {
            simberSpy.i(message = any())
        }
    }

    companion object {
        private const val TRACE_NAME = "testTrace"
    }

}
