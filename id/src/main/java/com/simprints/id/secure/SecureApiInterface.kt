package com.simprints.id.secure

import com.simprints.id.secure.models.remote.ApiSecurityState
import com.simprints.infra.network.SimRemoteInterface
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SecureApiInterface : SimRemoteInterface {
    @GET("projects/{projectId}/devices/{deviceId}")
    suspend fun requestSecurityState(
        @Path("projectId") projectId: String,
        @Path("deviceId") deviceId: String,
        @Query("previousInstructionId") previousInstructionId: String,
    ): ApiSecurityState

}
