package com.simprints.infra.config.store.remote

import com.simprints.infra.config.store.remote.models.ApiDeviceState
import com.simprints.infra.config.store.remote.models.ApiFileUrl
import com.simprints.infra.config.store.remote.models.ApiProject
import com.simprints.infra.network.SimRemoteInterface
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal interface ConfigRemoteInterface : SimRemoteInterface {
    @GET("projects/{projectId}")
    suspend fun getProject(
        @Path("projectId") projectId: String,
    ): ApiProject

    @GET("projects/{projectId}/devices/{deviceId}")
    suspend fun getDeviceState(
        @Path("projectId") projectId: String,
        @Path("deviceId") deviceId: String,
        @Query("previousInstructionId") previousInstructionId: String,
    ): ApiDeviceState

    @GET("projects/{projectId}/files/{fileId}")
    suspend fun getFileUrl(
        @Path("projectId") projectId: String,
        @Path("fileId") fileId: String,
    ): ApiFileUrl
}
