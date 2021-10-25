package com.simprints.logging

import com.google.firebase.perf.FirebasePerformance
import com.simprints.logging.performancemonitoring.PerformanceMonitoringTrace

object PerformanceMonitor {

    /**
     * Used to track the performance of something inside of the code. Call
     * [PerformanceMonitoringTrace] to get a [PerformanceMonitoringTrace] instance. Call
     * [PerformanceMonitoringTrace.start] to begin the trace and [PerformanceMonitoringTrace.stop]
     * to end it.
     * @param traceName The name of the trace
     * @param simber Optional [Simber] object to specify which object is used to log.
     * DEBUG: Is sent to Log.i
     * STAGING: Is sent to Log.i & sent to Firebase Performance Monitoring
     * RELEASE: Is sent to Firebase Performance Monitoring
     */
    fun trace(traceName: String, simber: Simber = Simber): PerformanceMonitoringTrace =
        getTrace(traceName, simber)

    internal fun getTrace(name: String, simber: Simber): PerformanceMonitoringTrace {
        return if (BuildConfig.DEBUG)
            PerformanceMonitoringTrace(name, null, simber)
        else
            PerformanceMonitoringTrace(name, FirebasePerformance.getInstance(), simber)
    }

}
