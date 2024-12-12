package com.simprints.infra.network

import retrofit2.http.GET

interface FakeRetrofitInterface : SimRemoteInterface {
    @GET("/path")
    suspend fun get(): Fake
}

data class Fake(
    val property: String,
)
