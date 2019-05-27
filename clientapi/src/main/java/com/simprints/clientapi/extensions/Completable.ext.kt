package com.simprints.clientapi.extensions

import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

fun Completable.inBackground(onError: (Throwable) -> Unit = { Timber.d(it.message) },
                             onComplete: () -> Unit = {}) =
    this.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeBy(onError = { Timber.d(it.message) })
