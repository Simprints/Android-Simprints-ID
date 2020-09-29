package com.simprints.id.secure

import com.simprints.id.network.SimRemoteInterface
import com.simprints.id.secure.models.AuthRequestBody
import com.simprints.id.secure.models.SecurityState
import com.simprints.id.secure.models.remote.ApiAuthenticationData
import com.simprints.id.secure.models.remote.ApiToken
import retrofit2.Response
import retrofit2.http.*

interface SecureApiInterface : SimRemoteInterface {

    @GET("projects/{projectId}/users/{userId}/authentication-data")
    suspend fun requestAuthenticationData(
        @Path("projectId") projectId: String,
        @Path("userId") userId: String,
        @Query("deviceId") deviceId: String
    ): Response<ApiAuthenticationData>

    @POST("projects/{projectId}/users/{userId}/authenticate")
    suspend fun requestCustomTokens(
        @Path("projectId") projectId: String,
        @Path("userId") userId: String,
        @Body credentials: AuthRequestBody
    ): Response<ApiToken>

    @GET("projects/{projectId}/devices/{deviceId}")
    suspend fun requestSecurityState(
        @Path("projectId") projectId: String,
        @Path("deviceId") deviceId: String
    ): Response<SecurityState>

}
