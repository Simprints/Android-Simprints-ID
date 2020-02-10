package com.simprints.id.data.db.project.remote

import com.google.gson.JsonElement
import com.simprints.core.network.NetworkConstants
import com.simprints.core.network.SimRemoteInterface
import com.simprints.id.data.db.project.domain.Project
import retrofit2.http.GET
import retrofit2.http.Path

interface ProjectRemoteInterface: SimRemoteInterface {

    companion object {
        const val baseUrl = NetworkConstants.baseUrl
    }

    @GET("projects/{projectId}")
    suspend fun requestProject(
        @Path("projectId") projectId: String): Project //TODO: it should return an ApiProject

    @GET("projects/{projectId}/config")
    suspend fun requestProjectConfig(
        @Path("projectId") projectId: String): JsonElement
}
