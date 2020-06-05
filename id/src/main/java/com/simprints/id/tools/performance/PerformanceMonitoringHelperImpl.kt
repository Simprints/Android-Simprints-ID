package com.simprints.id.tools.performance

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace

class PerformanceMonitoringHelperImpl : PerformanceMonitoringHelper {

    override fun startTrace(traceName: String?): Trace? {
        val trace = if (traceName != null)
            FirebasePerformance.getInstance().newTrace(traceName)
        else
            null

        return trace?.apply { start() }
    }

}
