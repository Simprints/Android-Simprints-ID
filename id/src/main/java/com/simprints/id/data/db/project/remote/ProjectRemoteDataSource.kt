package com.simprints.id.data.db.project.remote

import com.fasterxml.jackson.databind.JsonNode
import com.simprints.infra.network.SimApiClient
import com.simprints.id.data.db.project.domain.Project


interface ProjectRemoteDataSource {

    suspend fun loadProjectFromRemote(projectId: String): Project
    suspend fun loadProjectRemoteConfigSettingsJsonString(projectId: String): JsonNode
    suspend fun getProjectApiClient(): SimApiClient<ProjectRemoteInterface>
}
