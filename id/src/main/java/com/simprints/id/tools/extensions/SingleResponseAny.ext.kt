package com.simprints.id.tools.extensions

import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.adapter.rxjava2.Result
import java.io.IOException

fun <T> Single<out Response<T>>.handleResponse(handleResponseError: (HttpException) -> Unit): Single<T> =
    flatMap { response ->
        if (!response.isSuccessful) handleResponseError(HttpException(response))
        Single.just(response.body())
    }

fun Single<out Result<Void?>>.handleResult(handleResponseError: (HttpException) -> Unit) : Completable =
    flatMapCompletable { result ->
        if (result.isError) {
            result.response()
                ?.let {
                    handleResponseError(HttpException(it))
                }
            ?: throw IOException(result.error())
        }
        Completable.complete()
    }
