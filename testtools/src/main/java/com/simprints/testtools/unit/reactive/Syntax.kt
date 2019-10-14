package com.simprints.testtools.unit.reactive

import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.TestSubscriber

fun <T> Flowable<T>.testSubscribe(testSubscriber: TestSubscriber<T>) = this
    .subscribeOn(Schedulers.io())
    .observeOn(Schedulers.trampoline())
    .subscribe(testSubscriber)

fun <T> Flowable<T>.testSubscribe() = this
    .subscribeOn(Schedulers.io())
    .observeOn(Schedulers.trampoline())
    .test()

fun <T> TestSubscriber<T>.awaitCompletionWithNoErrors() {
    awaitTerminalEvent()
    assertComplete()
    assertNoErrors()
}
