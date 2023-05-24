package com.simprints.infra.authstore.remote

import com.simprints.infra.authstore.remote.models.ApiAuthRequestBody
import com.simprints.infra.authstore.remote.models.ApiAuthenticationData
import com.simprints.infra.authstore.remote.models.ApiToken
import com.simprints.infra.network.SimRemoteInterface
import retrofit2.http.*

internal interface AuthenticationRemoteInterface : SimRemoteInterface {

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
