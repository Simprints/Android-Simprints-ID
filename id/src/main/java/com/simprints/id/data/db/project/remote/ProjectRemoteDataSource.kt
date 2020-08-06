package com.simprints.id.data.db.project.remote

import com.fasterxml.jackson.databind.JsonNode
import com.google.gson.JsonElement
import com.simprints.id.data.db.project.domain.Project
import com.simprints.id.network.SimApiClient


interface ProjectRemoteDataSource {

    suspend fun loadProjectFromRemote(projectId: String): Project
    suspend fun loadProjectRemoteConfigSettingsJsonString(projectId: String): JsonNode
    suspend fun getProjectApiClient(): SimApiClient<ProjectRemoteInterface>
}
