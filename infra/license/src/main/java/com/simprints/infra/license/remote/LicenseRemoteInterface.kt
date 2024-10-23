package com.simprints.infra.license.remote

import com.simprints.infra.network.SimRemoteInterface
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal interface LicenseRemoteInterface : SimRemoteInterface {

    @GET("projects/{projectId}/devices/{deviceId}/licenses")
    suspend fun getLicense(
        @Path("projectId") projectId: String,
        @Path("deviceId") deviceId: String,
        @Query("vendor") vendor: String,
        @Query("version") version: String,
    ): String
}
