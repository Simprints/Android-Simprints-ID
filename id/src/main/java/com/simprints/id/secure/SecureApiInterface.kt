package com.simprints.id.secure

import com.simprints.core.network.SimRemoteInterface
import com.simprints.id.BuildConfig
import com.simprints.id.secure.models.AuthRequestBody
import com.simprints.id.secure.models.remote.ApiAuthenticationData
import com.simprints.id.secure.models.remote.ApiToken
import retrofit2.Response
import retrofit2.http.*

interface SecureApiInterface: SimRemoteInterface {
    companion object {
        const val apiKey: String = BuildConfig.ANDROID_AUTH_API_KEY
    }

    @GET("projects/{projectId}/users/{userId}/authentication-data")
    suspend fun requestAuthenticationData(@Path("projectId") projectId: String,
                                  @Path("userId") userId: String,
                                  @Query("key") key: String = SecureApiInterface.apiKey): Response<ApiAuthenticationData>

    @POST("projects/{projectId}/users/{userId}/authenticate")
    suspend fun requestCustomTokens(@Path("projectId") projectId: String,
                            @Path("userId") userId: String,
                            @Body credentials: AuthRequestBody,
                            @Query("key") key: String = SecureApiInterface.apiKey): Response<ApiToken>
}
