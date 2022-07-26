package com.simprints.infra.login.remote

import com.simprints.infra.login.remote.models.ApiAuthRequestBody
import com.simprints.infra.login.remote.models.ApiAuthenticationData
import com.simprints.infra.login.remote.models.ApiToken
import com.simprints.infra.network.SimRemoteInterface
import retrofit2.http.*

interface AuthenticationRemoteInterface : SimRemoteInterface {

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
        @Body credentials: ApiAuthRequestBody
    ): ApiToken

}
