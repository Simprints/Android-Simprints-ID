package com.simprints.id.tools.performance

import com.google.firebase.perf.metrics.Trace

interface PerformanceMonitoringHelper {

    fun startTrace(traceName: String?): Trace?

}
