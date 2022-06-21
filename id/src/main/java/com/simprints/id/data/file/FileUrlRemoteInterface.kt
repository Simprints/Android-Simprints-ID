package com.simprints.id.data.file

import com.simprints.core.network.SimRemoteInterface
import retrofit2.http.GET
import retrofit2.http.Path

interface FileUrlRemoteInterface: SimRemoteInterface {

    @GET("projects/{projectId}/files/{fileId}")
    suspend fun getFileUrl(
        @Path("projectId") projectId: String,
        @Path("fileId") fileId: String
    ): FileUrl

}
