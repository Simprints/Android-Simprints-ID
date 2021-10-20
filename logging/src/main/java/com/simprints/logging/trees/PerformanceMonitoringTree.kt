package com.simprints.logging.trees

import com.google.firebase.perf.FirebasePerformance
import com.simprints.logging.BuildConfig
import com.simprints.logging.Simber


internal object PerformanceMonitoringTree {

    var performanceMonitor: FirebasePerformance? = null

    fun getTrace(name: String) = Trace(name, performanceMonitor)

}


class Trace(val name: String, performanceMonitor: FirebasePerformance?) {

    private val startTime = System.currentTimeMillis()
    private val newTrace = performanceMonitor?.newTrace(name)

    fun start() {
        newTrace?.start()
    }

    fun stop() {
        newTrace?.stop()

        if (BuildConfig.DEBUG)
            Simber.i("Trace time for $name = ${System.currentTimeMillis() - startTime}")
    }

}




