package com.simprints.fingerprintscanner.v2.tools.reactive

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

fun <T> single(function: () -> T): Single<T> = Single.create { emitter ->
    try {
        val result = function.invoke()
        emitter.onSuccess(result)
    } catch (e: Throwable) {
        emitter.onError(e)
    }
}

fun completable(function: () -> Unit): Completable = Completable.fromAction {
    function.invoke()
}

inline fun <reified R> Flowable<*>.filterCast(
    crossinline predicate: (R) -> Boolean = { true }
) =
    this.filter { it is R && predicate(it) }
        .map { it as R }

fun Single<*>.completeOnceReceived(): Completable =
    this.ignoreElement()
