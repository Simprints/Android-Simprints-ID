package com.simprints.eventsystem.project.remote

import com.fasterxml.jackson.databind.JsonNode
import com.simprints.eventsystem.project.domain.Project
import com.simprints.id.network.SimRemoteInterface
import retrofit2.http.GET
import retrofit2.http.Path

interface ProjectRemoteInterface: SimRemoteInterface {

    @GET("projects/{projectId}")
    suspend fun requestProject(
        @Path("projectId") projectId: String): Project //TODO: it should return an ApiProject

    @GET("projects/{projectId}/config")
    suspend fun requestProjectConfig(
        @Path("projectId") projectId: String): JsonNode
}
