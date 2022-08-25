package com.simprints.infra.config.remote

import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.infra.login.LoginManager
import com.simprints.infra.network.SimNetwork
import javax.inject.Inject

internal class ConfigRemoteDataSourceImpl @Inject constructor(private val loginManager: LoginManager) :
    ConfigRemoteDataSource {

    override suspend fun getConfiguration(projectId: String): ProjectConfiguration =
        getApiClient().executeCall { it.getConfiguration(projectId) }.toDomain()

    override suspend fun getProject(projectId: String): Project =
        getApiClient().executeCall { it.getProject(projectId) }.toDomain()

    private suspend fun getApiClient(): SimNetwork.SimApiClient<ConfigRemoteInterface> =
        loginManager.buildClient(ConfigRemoteInterface::class)
}
