package com.simprints.testtools.unit.reactive

import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.TestSubscriber

fun <T> Flowable<T>.testSubscribe(): TestSubscriber<T> = this
    .subscribeOn(Schedulers.io())
    .observeOn(Schedulers.trampoline())
    .test()
