package com.simprints.infra.config.store.remote

import com.simprints.core.DispatcherIO
import com.simprints.infra.backendapi.BackendApiClient
import com.simprints.infra.config.store.models.DeviceState
import com.simprints.infra.config.store.models.ProjectWithConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.net.URL
import javax.inject.Inject

internal class ConfigRemoteDataSourceImpl(
    private val backendApiClient: BackendApiClient,
    @param:DispatcherIO private val dispatcherIO: CoroutineDispatcher,
    private val urlDownloader: (String) -> String,
) : ConfigRemoteDataSource {
    @Inject
    constructor(
        backendApiClient: BackendApiClient,
        @DispatcherIO dispatcherIO: CoroutineDispatcher,
    ) : this(backendApiClient, dispatcherIO, { url -> URL(url).readText() })

    override suspend fun getProject(projectId: String): ProjectWithConfig = backendApiClient
        .executeCall(ConfigRemoteInterface::class) { it.getProject(projectId) }
        .getOrThrow()
        .let { ProjectWithConfig(it.toDomain(), it.configuration.toDomain()) }

    override suspend fun getPrivacyNotice(
        projectId: String,
        fileId: String,
    ): String {
        val url = backendApiClient
            .executeCall(ConfigRemoteInterface::class) { it.getFileUrl(projectId, fileId) }
            .getOrThrow()
            .url

        return withContext(dispatcherIO) { urlDownloader(url) }
    }

    override suspend fun getDeviceState(
        projectId: String,
        deviceId: String,
        previousInstructionId: String,
    ): DeviceState = backendApiClient
        .executeCall(ConfigRemoteInterface::class) { it.getDeviceState(projectId, deviceId, previousInstructionId) }
        .getOrThrow()
        .toDomain()
}
