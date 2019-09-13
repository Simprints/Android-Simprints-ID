package com.simprints.core.tools

import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.runBlocking

fun completableWithSuspend(block: suspend () -> Unit): Completable =
    Completable.create {
        try {
            runBlocking { block() }
            it.onComplete()
        } catch (t: Throwable) {
            t.printStackTrace()
            it.onError(t)
        }
    }

fun <T> singleWithSuspend(block: suspend () -> T?) =
    Single.create<T> {
        try {
            it.onSuccess(runBlocking { block() }!!)
        } catch (t: Throwable) {
            t.printStackTrace()
            it.onError(t)
        }
    }
