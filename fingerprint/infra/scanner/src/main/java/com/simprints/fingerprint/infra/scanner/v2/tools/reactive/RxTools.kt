package com.simprints.fingerprint.infra.scanner.v2.tools.reactive

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.flowables.ConnectableFlowable
import io.reactivex.rxkotlin.Singles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx2.asScheduler

fun <T : Any> single(function: () -> T): Single<T> = Single.create { emitter ->
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

inline fun <reified R> Flowable<*>.filterCast() = this.filter { it is R }.map { it as R }

fun <T> Flowable<T>.subscribeOnIoAndPublish(): ConnectableFlowable<T> =
    this.subscribeOn(ioScheduler).publish()

fun <T : Any> Completable.doSimultaneously(single: Single<T>): Single<T> =
    Singles.zip(single, this.toSingleDefault(Unit)) { value, _ -> value }


//Todo Make it more generic by injecting scheduler once refactoring the scanner module
val ioScheduler = Dispatchers.IO.asScheduler()
