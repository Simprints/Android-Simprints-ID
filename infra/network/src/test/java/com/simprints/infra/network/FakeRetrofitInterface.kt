package com.simprints.infra.network

import kotlinx.serialization.Serializable
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface FakeRetrofitInterface : SimRemoteInterface {
    @GET("/path")
    suspend fun get(): Fake

    @GET("/response")
    suspend fun getResponse(): Response<ResponseBody>
}

@Serializable
data class Fake(
    val property: String,
)
