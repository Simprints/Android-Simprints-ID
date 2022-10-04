package com.simprints.id.data.db.project.remote

import com.fasterxml.jackson.databind.JsonNode
import com.simprints.id.data.db.project.domain.Project
import com.simprints.infra.network.SimRemoteInterface
import retrofit2.http.GET
import retrofit2.http.Path

interface ProjectRemoteInterface: SimRemoteInterface {

    @GET("projects/{projectId}")
    suspend fun requestProject(@Path("projectId") projectId: String): Project

    @GET("projects/{projectId}/config")
    suspend fun requestProjectConfig(@Path("projectId") projectId: String): JsonNode

}
