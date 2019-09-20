package com.simprints.core.tools.extentions

import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.runBlocking

fun completableWithSuspend(block: suspend () -> Unit): Completable =
    Completable.fromAction { runBlocking { block() } }

fun <T> singleWithSuspend(block: suspend () -> T?): Single<T> =
    Single.create<T> {
        try {
            it.onSuccess(runBlocking { block() }!!)
        } catch (t: Throwable) {
            t.printStackTrace()
            it.onError(t)
        }
    }
