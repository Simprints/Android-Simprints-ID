package com.simprints.id.secure

import com.simprints.id.secure.models.remote.ApiSecurityState
import com.simprints.infra.network.SimRemoteInterface
import retrofit2.http.GET
import retrofit2.http.Path

interface SecureApiInterface : SimRemoteInterface {
    @GET("projects/{projectId}/devices/{deviceId}")
    suspend fun requestSecurityState(
        @Path("projectId") projectId: String,
        @Path("deviceId") deviceId: String
    ): ApiSecurityState

}
