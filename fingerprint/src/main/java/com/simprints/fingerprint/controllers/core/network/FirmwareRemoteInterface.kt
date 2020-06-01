package com.simprints.fingerprint.controllers.core.network

import retrofit2.http.GET
import retrofit2.http.Query

@JvmSuppressWildcards
interface FirmwareRemoteInterface : FingerprintRemoteInterface {

    @GET("firmware/versions")
    suspend fun getAvailableDownloadableVersions(
        @Query("from") versionsGreaterThan: String? = null, // Defaults to latest versions only
        @Query("fields") desiredFields: String? = null // Defaults to all fields
    ): List<ApiFirmwareVersionResponse>
}
