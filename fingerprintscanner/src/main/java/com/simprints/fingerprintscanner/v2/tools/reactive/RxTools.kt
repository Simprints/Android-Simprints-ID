package com.simprints.fingerprintscanner.v2.tools.reactive

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.flowables.ConnectableFlowable
import io.reactivex.rxkotlin.Singles
import io.reactivex.schedulers.Schedulers

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

fun <T> Flowable<T>.subscribeOnIoAndPublish(): ConnectableFlowable<T> =
    this.subscribeOn(Schedulers.io()).publish()

fun <T> Completable.doSimultaneously(single: Single<T>): Single<T> =
    Singles.zip(single, this.toSingleDefault(Unit)) { value, _ -> value }
