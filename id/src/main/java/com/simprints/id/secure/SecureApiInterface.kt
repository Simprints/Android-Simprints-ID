package com.simprints.id.secure

import com.simprints.id.BuildConfig
import com.simprints.id.secure.models.Nonce
import com.simprints.id.secure.models.ProjectId
import com.simprints.id.secure.models.PublicKeyString
import com.simprints.id.secure.models.Tokens
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Query

interface SecureApiInterface {

    companion object {
        private const val apiVersion = "2018-2-0"
        const val baseUrl = "https://$apiVersion-dot-project-manager-dot-${BuildConfig.GCP_PROJECT}.appspot.com"
        private const val apiKey: String = "AIzaSyAORPo9YH-TBw0F1ch8BMP9IGkNElgon6s"
    }

    @GET("/project-ids")
    fun projectId(@HeaderMap headers: Map<String, String>, //legacyIdMd5
                  @Query("key") key: String = SecureApiInterface.apiKey): Single<Response<ProjectId>>

    @GET("/nonces")
    fun nonce(@HeaderMap headers: Map<String, String>,
              @Query("key") key: String = SecureApiInterface.apiKey): Single<Response<Nonce>>

    @GET("/public-key")
    fun publicKey(@Query("key") key: String = SecureApiInterface.apiKey): Single<Response<PublicKeyString>>

    @GET("/tokens")
    fun auth(@HeaderMap headers: Map<String, String>,
             @Query("key") key: String = SecureApiInterface.apiKey): Single<Response<Tokens>>
}
