package com.simprints.id.secure

import com.simprints.id.BuildConfig
import com.simprints.id.network.NetworkConstants
import com.simprints.id.secure.models.*
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.*

interface SecureApiInterface {
    companion object {
        const val baseUrl = NetworkConstants.baseUrl
        const val apiKey: String = BuildConfig.ANDROID_AUTH_API_KEY
    }

    @GET("legacy-projects/{legacyIdMD5}")
    fun legacyProject(@Path("legacyIdMD5") legacyIdMD5: String,
                      @Query("key") key: String = SecureApiInterface.apiKey): Single<Response<LegacyProject>>

    @POST("projects/{projectId}/users/{userId}/nonces")
    fun nonce(@Path("projectId") projectId: String,
              @Path("userId") userId: String,
              @Query("key") key: String = SecureApiInterface.apiKey): Single<Response<Nonce>>

    @GET("projects/{projectId}/users/{userId}/public-key")
    fun publicKey(@Path("projectId") projectId: String,
                  @Path("userId") userId: String,
                  @Query("key") key: String = SecureApiInterface.apiKey): Single<Response<PublicKeyString>>

    @POST("projects/{projectId}/users/{userId}/custom-tokens")
    fun customTokens(@Path("projectId") projectId: String,
                     @Path("userId") userId: String,
                     @Body credentials: AuthRequestBody,
                     @Query("key") key: String = SecureApiInterface.apiKey): Single<Response<Tokens>>
}
