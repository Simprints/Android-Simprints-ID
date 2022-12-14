package com.simprints.infra.config.remote

import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.infra.login.LoginManager
import com.simprints.infra.network.SimNetwork
import java.net.URL
import javax.inject.Inject

internal class ConfigRemoteDataSourceImpl(
    private val loginManager: LoginManager,
    private val urlDownloader: (String) -> String
) :
    ConfigRemoteDataSource {

    @Inject
    constructor(loginManager: LoginManager) : this(loginManager, { url -> URL(url).readText() })

    override suspend fun getConfiguration(projectId: String): ProjectConfiguration =
        getApiClient().executeCall { it.getConfiguration(projectId) }.toDomain()

    override suspend fun getProject(projectId: String): Project =
        getApiClient().executeCall { it.getProject(projectId) }.toDomain()

    override suspend fun getPrivacyNotice(projectId: String, fileId: String): String =
        getApiClient().executeCall { it.getFileUrl(projectId, fileId) }.url.let {
            urlDownloader(it)
        }

    private suspend fun getApiClient(): SimNetwork.SimApiClient<ConfigRemoteInterface> =
        loginManager.buildClient(ConfigRemoteInterface::class)
}
