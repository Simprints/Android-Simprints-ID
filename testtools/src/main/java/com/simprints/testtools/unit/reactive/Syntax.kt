package com.simprints.testtools.unit.reactive

import io.reactivex.*
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.TestSubscriber

fun <T> Flowable<T>.testSubscribe(observeOn: Scheduler = Schedulers.trampoline()): TestSubscriber<T> = this
    .subscribeOn(Schedulers.io())
    .observeOn(observeOn)
    .test()

fun <T> Observable<T>.testSubscribe(observeOn: Scheduler = Schedulers.trampoline()): TestObserver<T> = this
    .subscribeOn(Schedulers.io())
    .observeOn(observeOn)
    .test()

fun <T> Single<T>.testSubscribe(observeOn: Scheduler = Schedulers.trampoline()): TestObserver<T> = this
    .subscribeOn(Schedulers.io())
    .observeOn(observeOn)
    .test()

fun Completable.testSubscribe(observeOn: Scheduler = Schedulers.trampoline()): TestObserver<Void> = this
    .subscribeOn(Schedulers.io())
    .observeOn(observeOn)
    .test()

fun <T> Maybe<T>.testSubscribe(observeOn: Scheduler = Schedulers.trampoline()): TestObserver<T> = this
    .subscribeOn(Schedulers.io())
    .observeOn(observeOn)
    .test()
