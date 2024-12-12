package com.simprints.infra.authlogic.authenticator.remote

import com.simprints.infra.authlogic.authenticator.remote.models.ApiAuthRequestBody
import com.simprints.infra.authlogic.authenticator.remote.models.ApiAuthenticationData
import com.simprints.infra.authlogic.authenticator.remote.models.ApiToken
import com.simprints.infra.network.SimRemoteInterface
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

internal interface AuthenticationRemoteInterface : SimRemoteInterface {
    @GET("projects/{projectId}/devices/{deviceId}/authentication-data")
    suspend fun requestAuthenticationData(
        @Path("projectId") projectId: String,
        @Path("deviceId") deviceId: String,
    ): ApiAuthenticationData

    @POST("projects/{projectId}/devices/{deviceId}/authenticate")
    suspend fun requestCustomTokens(
        @Path("projectId") projectId: String,
        @Path("deviceId") deviceId: String,
        @Body credentials: ApiAuthRequestBody,
    ): ApiToken
}
