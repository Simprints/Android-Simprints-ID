package com.simprints.infra.config.store.remote

import com.simprints.infra.config.store.remote.models.ApiFileUrl
import com.simprints.infra.config.store.remote.models.ApiProject
import com.simprints.infra.config.store.remote.models.ApiProjectConfiguration
import com.simprints.infra.network.SimRemoteInterface
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

internal interface ConfigRemoteInterface : SimRemoteInterface {

    @GET("projects/{projectId}/configuration")
    suspend fun getConfiguration(@Path("projectId") projectId: String): ApiProjectConfiguration

    // TODO Remove once all sync-revamp API changes have been implemented
    @Headers("X-Force-Version: 2024.1.1")
    @GET("projects/{projectId}")
    suspend fun getProject(@Path("projectId") projectId: String): ApiProject

    @GET("projects/{projectId}/files/{fileId}")
    suspend fun getFileUrl(
        @Path("projectId") projectId: String,
        @Path("fileId") fileId: String
    ): ApiFileUrl
}
