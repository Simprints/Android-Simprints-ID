package com.simprints.id.secure

import com.simprints.id.secure.models.AuthRequestBody
import com.simprints.id.secure.models.remote.ApiAuthenticationData
import com.simprints.id.secure.models.remote.ApiSecurityState
import com.simprints.id.secure.models.remote.ApiToken
import com.simprints.infra.network.SimRemoteInterface
import retrofit2.http.*

interface SecureApiInterface : SimRemoteInterface {

    @GET("projects/{projectId}/users/{userId}/authentication-data")
    suspend fun requestAuthenticationData(
        @Path("projectId") projectId: String,
        @Path("userId") userId: String,
        @Query("deviceId") deviceId: String
    ): ApiAuthenticationData

    @POST("projects/{projectId}/users/{userId}/authenticate")
    suspend fun requestCustomTokens(
        @Path("projectId") projectId: String,
        @Path("userId") userId: String,
        @Body credentials: AuthRequestBody
    ): ApiToken

    @GET("projects/{projectId}/devices/{deviceId}")
    suspend fun requestSecurityState(
        @Path("projectId") projectId: String,
        @Path("deviceId") deviceId: String
    ): ApiSecurityState

}
