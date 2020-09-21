package com.simprints.id.tools.extensions

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace

interface FirebasePerformanceTraceFactory {

    fun newTrace(traceName: String): Trace
}

class FirebasePerformanceTraceFactoryImpl: FirebasePerformanceTraceFactory {

    override fun newTrace(traceName: String) =
        FirebasePerformance.getInstance().newTrace(traceName)
}
