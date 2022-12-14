package com.simprints.infra.config.remote

import com.simprints.infra.config.remote.models.ApiFileUrl
import com.simprints.infra.config.remote.models.ApiProject
import com.simprints.infra.config.remote.models.ApiProjectConfiguration
import com.simprints.infra.network.SimRemoteInterface
import retrofit2.http.GET
import retrofit2.http.Path

internal interface ConfigRemoteInterface : SimRemoteInterface {

    @GET("projects/{projectId}/configuration")
    suspend fun getConfiguration(@Path("projectId") projectId: String): ApiProjectConfiguration

    @GET("projects/{projectId}")
    suspend fun getProject(@Path("projectId") projectId: String): ApiProject

    @GET("projects/{projectId}/files/{fileId}")
    suspend fun getFileUrl(
        @Path("projectId") projectId: String,
        @Path("fileId") fileId: String
    ): ApiFileUrl
}
