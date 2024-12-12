package com.simprints.infra.config.store.remote

import com.simprints.core.DispatcherIO
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.DeviceState
import com.simprints.infra.config.store.models.ProjectWithConfig
import com.simprints.infra.network.SimNetwork
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.net.URL
import javax.inject.Inject

internal class ConfigRemoteDataSourceImpl(
    private val authStore: AuthStore,
    @DispatcherIO private val dispatcherIO: CoroutineDispatcher,
    private val urlDownloader: (String) -> String,
) : ConfigRemoteDataSource {
    @Inject
    constructor(
        authStore: AuthStore,
        @DispatcherIO dispatcherIO: CoroutineDispatcher,
    ) : this(authStore, dispatcherIO, { url -> URL(url).readText() })

    override suspend fun getProject(projectId: String): ProjectWithConfig = getApiClient()
        .executeCall { it.getProject(projectId) }
        .let { ProjectWithConfig(it.toDomain(), it.configuration.toDomain()) }

    override suspend fun getPrivacyNotice(
        projectId: String,
        fileId: String,
    ): String {
        val url = getApiClient().executeCall { it.getFileUrl(projectId, fileId) }.url
        return withContext(dispatcherIO) { urlDownloader(url) }
    }

    override suspend fun getDeviceState(
        projectId: String,
        deviceId: String,
        previousInstructionId: String,
    ): DeviceState = getApiClient()
        .executeCall { it.getDeviceState(projectId, deviceId, previousInstructionId) }
        .toDomain()

    private suspend fun getApiClient(): SimNetwork.SimApiClient<ConfigRemoteInterface> = authStore.buildClient(ConfigRemoteInterface::class)
}
