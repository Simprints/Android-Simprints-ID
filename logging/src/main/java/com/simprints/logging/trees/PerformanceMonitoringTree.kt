package com.simprints.logging.trees

import com.google.firebase.perf.FirebasePerformance
import com.simprints.logging.BuildConfig
import com.simprints.logging.Simber


internal object PerformanceMonitoringTree {

    var performanceMonitor: FirebasePerformance? = null

    fun getTrace(name: String, simber: Simber) = Trace(name, performanceMonitor, simber)

}


class Trace(val name: String, performanceMonitor: FirebasePerformance?, val simber: Simber) {

    private var startTime: Long? = null
    private val newTrace = performanceMonitor?.newTrace(name)

    fun start() {
        startTime = System.currentTimeMillis()
        newTrace?.start()
    }

    fun stop() {
        newTrace?.stop()

        if (BuildConfig.DEBUG)
            startTime?.let {
                simber.i("Trace time for $name = ${System.currentTimeMillis() - it} ms")
            }
    }

}




