package com.simprints.infra.network

import kotlinx.serialization.Serializable
import retrofit2.http.GET

interface FakeRetrofitInterface : SimRemoteInterface {
    @GET("/path")
    suspend fun get(): Fake
}

@Serializable
data class Fake(
    val property: String,
)
