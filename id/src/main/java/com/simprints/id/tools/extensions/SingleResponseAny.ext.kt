package com.simprints.id.tools.extensions

import io.reactivex.Single
import retrofit2.HttpException
import retrofit2.Response

fun <T> Single<out Response<T>>.handleResponse(handleResponseError: (HttpException) -> Unit): Single<T> =
    flatMap { response ->
        if (!response.isSuccessful) handleResponseError(HttpException(response))
        Single.just(response.body())
    }
