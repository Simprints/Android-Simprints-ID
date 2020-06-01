package com.simprints.fingerprint.controllers.core.network

import retrofit2.http.GET
import retrofit2.http.Query

@JvmSuppressWildcards
interface FirmwareRemoteInterface : FingerprintRemoteInterface {

    @GET("firmware/versions")
    suspend fun getAvailableDownloadableVersions(
        @Query("from-cypress") aboveCypressVersion: String,
        @Query("from-stm") aboveStmVersion: String,
        @Query("from-un20") aboveUn20Version: String,
        @Query("fields") desiredFields: String? = null // Defaults to all fields
    ): Map<String, ApiFirmwareVersionResponse>
}
