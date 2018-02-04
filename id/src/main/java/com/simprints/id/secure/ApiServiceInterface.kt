package com.simprints.id.secure

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Query

interface ApiServiceInterface {

    @GET("/nonces")
    fun nonce(@HeaderMap headers: Map<String, String>, @Query("key") key: String): Single<String>
}
