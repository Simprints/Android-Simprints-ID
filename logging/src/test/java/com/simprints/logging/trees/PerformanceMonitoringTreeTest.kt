package com.simprints.logging.trees

import com.google.firebase.perf.FirebasePerformance
import com.simprints.logging.BuildConfig
import com.simprints.logging.Simber
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test
import org.robolectric.util.ReflectionHelpers

class PerformanceMonitoringTreeTest {

    @Test
    fun `calling trace start should try to create a new FPM trace`() {
        val fpmMock: FirebasePerformance = mockk(relaxed = true)
        PerformanceMonitoringTree.performanceMonitor = fpmMock

        Simber.trace(TRACE_NAME)

        verify { fpmMock.newTrace(TRACE_NAME) }
    }

    @Test
    fun `calling trace should get new trace`() {
        PerformanceMonitoringTree.performanceMonitor = mockk(relaxed = true)

        Simber.trace(TRACE_NAME)

        verify { PerformanceMonitoringTree.getTrace(TRACE_NAME, Simber) }
    }

    @Test
    fun `if FPM isn't set trace should not be created`() {
        val fpmMock: FirebasePerformance = mockk(relaxed = true)

        Simber.trace(TRACE_NAME)

        verify(exactly = 0) { fpmMock.newTrace(TRACE_NAME) }
        assert(PerformanceMonitoringTree.performanceMonitor == null)
    }

    @Test
    fun `if FPM is set trace start should start FPM trace`() {
        val fpmMock: FirebasePerformance = mockk(relaxed = true)
        PerformanceMonitoringTree.performanceMonitor = fpmMock

        Simber.trace(TRACE_NAME).start()

        verify { fpmMock.newTrace(TRACE_NAME).start() }
    }

    @Test
    fun `if FPM is set trace stop should stop FPM trace`() {
        val fpmMock: FirebasePerformance = mockk(relaxed = true)
        PerformanceMonitoringTree.performanceMonitor = fpmMock

        Simber.trace(TRACE_NAME).stop()

        verify { fpmMock.newTrace(TRACE_NAME).stop() }
    }

    @Test
    fun `if FPM isn't set trace start should not start FPM trace`() {
        val fpmMock: FirebasePerformance = mockk(relaxed = true)

        Simber.trace(TRACE_NAME).start()

        verify(exactly = 0) { fpmMock.newTrace(TRACE_NAME).start() }
    }

    @Test
    fun `if FPM isn't set trace stop should not stop FPM trace`() {
        val fpmMock: FirebasePerformance = mockk(relaxed = true)

        Simber.trace(TRACE_NAME).stop()

        verify(exactly = 0) { fpmMock.newTrace(TRACE_NAME).stop() }
    }

    @Test
    fun `if debug mode stopping trace should print time`() {
        ReflectionHelpers.setStaticField(BuildConfig::class.java, "DEBUG", true)
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
        ReflectionHelpers.setStaticField(BuildConfig::class.java, "DEBUG", false)
        val simberSpy = spyk<Simber>()

        val trace = simberSpy.trace(TRACE_NAME)
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
