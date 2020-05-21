package com.simprints.id.data.db.project.remote

import com.google.gson.JsonElement
import com.simprints.id.data.db.project.domain.Project
import com.simprints.id.network.SimApiClient


interface ProjectRemoteDataSource {

    suspend fun loadProjectFromRemote(projectId: String): Project
    suspend fun loadProjectRemoteConfigSettingsJsonString(projectId: String): JsonElement
    suspend fun getProjectApiClient(): SimApiClient<ProjectRemoteInterface>
}
