package com.simprints.id.tools.extensions

import com.google.android.gms.tasks.Task
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

private const val CALL_SUCCESS_NAME = "call_success"
private const val FAILURE: Long = 0
private const val SUCCESS: Long = 1

fun Completable.trace(traceName: String): Completable {
    val trace = FirebasePerformance.getInstance().newTrace(traceName)
    return this.doOnSubscribe { trace.start() }
        .doOnComplete { trace.setSuccess(true) }
        .doOnError { trace.setSuccess(false) }
        .doFinally { trace.stop() }
}

fun <T> Single<T>.trace(traceName: String): Single<T> {
    val trace = FirebasePerformance.getInstance().newTrace(traceName)
    return this.doOnSubscribe { trace.start() }
        .doOnSuccess { trace.setSuccess(true) }
        .doOnError { trace.setSuccess(false) }
        .doFinally { trace.stop() }
}

fun <T> Observable<T>.trace(traceName: String): Observable<T> {
    val trace = FirebasePerformance.getInstance().newTrace(traceName)
    return this.doOnSubscribe { trace.start() }
        .doOnComplete { trace.setSuccess(true) }
        .doOnError { trace.setSuccess(false) }
        .doFinally { trace.stop() }
}

fun <T> Task<T>.trace(traceName: String): Task<T> {
    val trace = FirebasePerformance.getInstance().newTrace(traceName)
    trace.start()
    return this.addOnCompleteListener { trace.setSuccess(it.isSuccessful); trace.stop() }
}

private fun Trace.setSuccess(success: Boolean) =
    if (success) {
        this.incrementMetric(CALL_SUCCESS_NAME, SUCCESS)
    }
    else {
        this.incrementMetric(CALL_SUCCESS_NAME, FAILURE)
    }



