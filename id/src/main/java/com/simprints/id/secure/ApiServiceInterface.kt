package com.simprints.id.secure

import com.simprints.id.BuildConfig
import com.simprints.id.secure.models.Nonce
import com.simprints.id.secure.models.PublicKeyString
import com.simprints.id.secure.models.Tokens
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Query

interface ApiServiceInterface {

    companion object {
        private const val apiVersion = "2018-1-0-dev5"
        const val baseUrl = "https://$apiVersion-dot-project-manager-dot-${BuildConfig.GCP_PROJECT}.appspot.com"
        const val apiKey: String = "AIzaSyAORPo9YH-TBw0F1ch8BMP9IGkNElgon6s"
    }

    @GET("/nonces")
    fun nonce(@HeaderMap headers: Map<String, String>,
              @Query("key") key: String = ApiServiceInterface.apiKey): Single<Nonce>

    @GET("/public-key")
    fun publicKey(@Query("key") key: String = ApiServiceInterface.apiKey): Single<PublicKeyString>

    @GET("/tokens")
    fun auth(@HeaderMap headers: Map<String, String>,
             @Query("key") key: String = ApiServiceInterface.apiKey): Single<Tokens>
}
