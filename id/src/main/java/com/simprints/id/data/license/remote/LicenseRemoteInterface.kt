package com.simprints.id.data.license.remote

import com.simprints.id.network.SimRemoteInterface
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LicenseRemoteInterface : SimRemoteInterface {
    @GET("projects/{projectId}/devices/{deviceId}/licenses")
    suspend fun getLicense(
        @Path("projectId") projectId: String,
        @Path("deviceId") deviceId: String,
        @Query("vendor") vendor: String?
    ): ApiLicense
}
