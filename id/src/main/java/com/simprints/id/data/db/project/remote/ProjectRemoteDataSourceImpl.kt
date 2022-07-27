package com.simprints.id.data.db.project.remote

import com.fasterxml.jackson.databind.JsonNode
import com.simprints.infra.network.SimApiClient
import com.simprints.id.data.db.project.domain.Project
import com.simprints.infra.login.network.SimApiClientFactory

open class ProjectRemoteDataSourceImpl(
    private val simApiClientFactory: SimApiClientFactory
) : ProjectRemoteDataSource {

    override suspend fun loadProjectFromRemote(projectId: String): Project =
        executeCall {
            it.requestProject(projectId)
        }

    override suspend fun loadProjectRemoteConfigSettingsJsonString(projectId: String): JsonNode =
        executeCall {
            it.requestProjectConfig(projectId)
        }

    private suspend fun <T> executeCall(block: suspend (ProjectRemoteInterface) -> T): T =
        with(getProjectApiClient()) {
            executeCall {
                block(it)
            }
        }

    override suspend fun getProjectApiClient(): SimApiClient<ProjectRemoteInterface> =
        simApiClientFactory.buildClient(ProjectRemoteInterface::class)
}
