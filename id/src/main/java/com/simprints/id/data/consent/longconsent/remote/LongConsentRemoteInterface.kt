package com.simprints.id.data.consent.longconsent.remote

import com.simprints.core.network.SimRemoteInterface
import retrofit2.http.GET
import retrofit2.http.Path

interface LongConsentRemoteInterface: SimRemoteInterface {

    @GET("projects/{projectId}/files/{fileId}")
    suspend fun getLongConsentDownloadUrl(
        @Path("projectId") projectId: String,
        @Path("fileId") fileId: String
    ): FileUrl

}
