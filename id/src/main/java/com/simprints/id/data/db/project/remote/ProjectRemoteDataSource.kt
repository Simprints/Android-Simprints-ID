package com.simprints.id.data.db.project.remote

import com.fasterxml.jackson.databind.JsonNode
import com.simprints.id.data.db.project.domain.Project
import com.simprints.infra.network.SimNetwork


interface ProjectRemoteDataSource {

    suspend fun loadProjectFromRemote(projectId: String): Project
    suspend fun loadProjectRemoteConfigSettingsJsonString(projectId: String): JsonNode
    suspend fun getProjectApiClient(): SimNetwork.SimApiClient<ProjectRemoteInterface>
}
