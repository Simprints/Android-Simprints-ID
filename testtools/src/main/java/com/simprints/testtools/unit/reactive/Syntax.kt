package com.simprints.testtools.unit.reactive

import io.reactivex.*
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.TestSubscriber

fun <T> Flowable<T>.testSubscribe(): TestSubscriber<T> = this
    .subscribeOn(Schedulers.io())
    .observeOn(Schedulers.trampoline())
    .test()

fun <T> Observable<T>.testSubscribe(): TestObserver<T> = this
    .subscribeOn(Schedulers.io())
    .observeOn(Schedulers.trampoline())
    .test()

fun <T> Single<T>.testSubscribe(): TestObserver<T> = this
    .subscribeOn(Schedulers.io())
    .observeOn(Schedulers.trampoline())
    .test()

fun Completable.testSubscribe(): TestObserver<Void> = this
    .subscribeOn(Schedulers.io())
    .observeOn(Schedulers.trampoline())
    .test()

fun <T> Maybe<T>.testSubscribe(): TestObserver<T> = this
    .subscribeOn(Schedulers.io())
    .observeOn(Schedulers.trampoline())
    .test()
