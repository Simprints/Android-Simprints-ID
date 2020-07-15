package com.simprints.face.license.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface SimprintsLicenseServer {
    companion object {
        const val BASE_URL = "https://sls.simprints-apis.com/"
    }
    @GET("license/{projectId}/{deviceId}")
    suspend fun getLicense(@Path("projectId") projectId: String, @Path("deviceId") deviceId: String): String
}
