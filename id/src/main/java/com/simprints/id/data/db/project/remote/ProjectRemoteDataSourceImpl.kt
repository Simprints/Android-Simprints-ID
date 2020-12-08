package com.simprints.id.data.db.project.remote

import com.fasterxml.jackson.databind.JsonNode
import com.simprints.id.data.db.project.domain.Project
import com.simprints.id.network.SimApiClient
import com.simprints.id.network.SimApiClientFactory

open class ProjectRemoteDataSourceImpl(
    private val simApiClientFactory: SimApiClientFactory
) : ProjectRemoteDataSource {

    override suspend fun loadProjectFromRemote(projectId: String): Project =
        executeCall("requestProject") {
            it.requestProject(projectId)
        }

    override suspend fun loadProjectRemoteConfigSettingsJsonString(projectId: String): JsonNode =
        executeCall("requestProjectConfig") {
            it.requestProjectConfig(projectId)
        }

    private suspend fun <T> executeCall(nameCall: String, block: suspend (ProjectRemoteInterface) -> T): T =
        with(getProjectApiClient()) {
            executeCall(nameCall) {
                block(it)
            }
        }

    override suspend fun getProjectApiClient(): SimApiClient<ProjectRemoteInterface> =
        simApiClientFactory.buildClient(ProjectRemoteInterface::class)
}
