package com.simprints.infra.config.remote

import com.simprints.core.DispatcherIO
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.infra.login.LoginManager
import com.simprints.infra.network.SimNetwork
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class ConfigRemoteDataSourceImpl(
    private val loginManager: LoginManager,
    @DispatcherIO private val dispatcherIO: CoroutineDispatcher,
    private val urlDownloader: (String) -> String
) :
    ConfigRemoteDataSource {

    override suspend fun getConfiguration(projectId: String): ProjectConfiguration =
        getApiClient().executeCall { it.getConfiguration(projectId) }.toDomain()

    override suspend fun getProject(projectId: String): Project =
        getApiClient().executeCall { it.getProject(projectId) }.toDomain()

    override suspend fun getPrivacyNotice(projectId: String, fileId: String): String {
        val url = getApiClient().executeCall { it.getFileUrl(projectId, fileId) }.url
        return withContext(dispatcherIO) { urlDownloader(url) }
    }

    private suspend fun getApiClient(): SimNetwork.SimApiClient<ConfigRemoteInterface> =
        loginManager.buildClient(ConfigRemoteInterface::class)
}
