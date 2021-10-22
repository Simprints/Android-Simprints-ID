package com.simprints.logging.trees

import com.google.firebase.perf.FirebasePerformance
import com.simprints.logging.BuildConfig
import com.simprints.logging.Simber


class PerformanceMonitoringTrace(
    val name: String,
    performanceMonitor: FirebasePerformance?,
    val simber: Simber
) {

    private var startTime: Long? = null
    internal val newTrace = performanceMonitor?.newTrace(name)

    /**
     * Call [start] to begin a new trace. If you call start again before calling [stop] to timer
     * will be reset to 0.
     */
    fun start() {
        startTime = System.currentTimeMillis()
        newTrace?.start()
    }

    /**
     * Call [stop] to end and report the current trace. Will be printed to [Simber.i] in debug mode.
     */
    fun stop() {
        newTrace?.stop()

        if (BuildConfig.DEBUG)
            startTime?.let {
                simber.i("Trace time for $name = ${System.currentTimeMillis() - it} ms")
            }
    }

}




