package com.simprints.logging

import com.google.firebase.perf.FirebasePerformance

//class PerformanceMonitoringTree(val pm: FirebasePerformance) : Timber.Tree() {
//
//    internal val traceForest: MutableSet<Traces> = mutableSetOf()
//
//    data class Traces(val tag: String, val trace: Trace) {
//
//        override fun equals(other: Any?): Boolean = (other is Traces) && tag == other.tag
//
//        override fun hashCode(): Int = tag.hashCode()
//
//    }
//
//    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
//        if (priority != Log.INFO || tag.isNullOrBlank() || !tag.contains(PERFORMANCE_FLAG)) return
//
//        if (tag.contains(START_FLAG)) {
//
//            val newTrace = pm.newTrace(message)
//            newTrace.start()
//            traceForest.add(Traces(message, newTrace))
//
//        } else if (tag.contains(STOP_FLAG)) {
//
//            traceForest.find { it.tag == message }?.let {
//                it.trace.stop()
//                traceForest.remove(it)
//            }
//
//        }
//
//    }
//
//    companion object {
//        internal const val START_FLAG = "START"
//        internal const val STOP_FLAG = "STOP"
//    }
//
//}

internal var performanceMonitor: FirebasePerformance? = null

fun Simber.trace(name: String, function: () -> (Unit)) {

    val startTime = System.currentTimeMillis()
    val newTrace = performanceMonitor?.newTrace(name)
    newTrace?.start()

    function.invoke()

    newTrace?.stop()
    this.i("Trace time for $name = ${System.currentTimeMillis() - startTime}")

}
