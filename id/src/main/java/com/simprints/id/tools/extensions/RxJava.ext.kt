package com.simprints.id.tools.extensions

import com.google.android.gms.tasks.Task
import com.google.firebase.perf.FirebasePerformance
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

private const val SUCCESS = "call_succeeded"
private const val FAILED = "call_failed"

fun Completable.trace(traceName: String): Completable {
    val trace = FirebasePerformance.getInstance().newTrace(traceName)
    return this.doOnSubscribe { trace.start() }
        .doOnComplete { trace.incrementCounter(SUCCESS) }
        .doOnError { trace.incrementCounter(FAILED) }
        .doFinally { trace.stop() }
}

fun <T> Single<T>.trace(traceName: String): Single<T> {
    val trace = FirebasePerformance.getInstance().newTrace(traceName)
    return this.doOnSubscribe { trace.start() }
        .doOnSuccess { trace.incrementCounter(SUCCESS) }
        .doOnError { trace.incrementCounter(FAILED) }
        .doFinally { trace.stop() }
}

fun <T> Observable<T>.trace(traceName: String): Observable<T> {
    val trace = FirebasePerformance.getInstance().newTrace(traceName)
    return this.doOnSubscribe { trace.start() }
        .doOnComplete { trace.incrementCounter(SUCCESS) }
        .doOnError { trace.incrementCounter(FAILED) }
        .doFinally { trace.stop() }
}

fun <T> Task<T>.trace(traceName: String): Task<T> {
    val trace = com.google.firebase.perf.FirebasePerformance.getInstance().newTrace(traceName)
    trace.start()
    return this.addOnSuccessListener { trace.incrementCounter(com.simprints.id.tools.extensions.SUCCESS) }
        .addOnFailureListener { trace.incrementCounter(com.simprints.id.tools.extensions.FAILED) }
        .addOnCompleteListener { trace.stop() }
}


