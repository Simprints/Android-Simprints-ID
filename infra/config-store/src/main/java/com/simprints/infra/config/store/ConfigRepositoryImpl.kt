package com.simprints.infra.config.store

import androidx.annotation.VisibleForTesting
import com.simprints.core.DeviceID
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.local.ConfigLocalDataSource
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.store.models.DeviceState
import com.simprints.infra.config.store.models.PrivacyNoticeResult
import com.simprints.infra.config.store.models.PrivacyNoticeResult.Failed
import com.simprints.infra.config.store.models.PrivacyNoticeResult.FailedBecauseBackendMaintenance
import com.simprints.infra.config.store.models.PrivacyNoticeResult.InProgress
import com.simprints.infra.config.store.models.PrivacyNoticeResult.Succeed
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.ProjectWithConfig
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.remote.ConfigRemoteDataSource
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ConfigRepositoryImpl @Inject constructor(
    private val authStore: AuthStore,
    private val localDataSource: ConfigLocalDataSource,
    private val remoteDataSource: ConfigRemoteDataSource,
    private val simNetwork: SimNetwork,
    private val tokenizationProcessor: TokenizationProcessor,
    private val configSyncCache: ConfigSyncCache,
    @param:DeviceID private val deviceId: String,
) : ConfigRepository {
    private val isProjectRefreshingFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override suspend fun getProject(): Project? = try {
        localDataSource.getProject()
    } catch (_: NoSuchElementException) {
        val projectId = authStore.signedInProjectId
        if (projectId.isEmpty()) {
            null
        } else {
            try {
                refreshProject(projectId).project
            } catch (_: Exception) {
                null
            }
        }
    }

    override suspend fun refreshProject(projectId: String): ProjectWithConfig {
        isProjectRefreshingFlow.tryEmit(true)
        return try {
            remoteDataSource
                .getProject(projectId)
                .also { (project, configuration) ->
                    localDataSource.saveProject(project)
                    localDataSource.saveProjectConfiguration(configuration)

                    if (!project.baseUrl.isNullOrBlank()) {
                        simNetwork.setApiBaseUrl(project.baseUrl)
                    }
                    configSyncCache.saveUpdateTime()
                }
        } finally {
            isProjectRefreshingFlow.tryEmit(false)
        }
    }

    override fun observeIsProjectRefreshing(): Flow<Boolean> = isProjectRefreshingFlow.asStateFlow()

    override suspend fun getProjectConfiguration(): ProjectConfiguration {
        val localConfig = localDataSource.getProjectConfiguration()

        // If projectId is empty, configuration hasn't been downloaded yet
        val config = if (localConfig.projectId.isEmpty()) {
            val signedProjectId = authStore.signedInProjectId.takeUnless { it.isEmpty() } ?: return localConfig

            try {
                // Try to refresh it with logged in projectId (if any)
                refreshProject(signedProjectId).configuration
            } catch (_: Exception) {
                // If not logged in the above will fail. However we still depend on the 'default'
                // configuration to create the session when login is attempted. Possibly in other
                // places, too.
                localConfig
            }
        } else {
            localConfig
        }

        return tokenizeModules(config)
    }

    override fun observeProjectConfiguration(): Flow<ProjectConfiguration> = localDataSource
        .observeProjectConfiguration()
        .onStart { getProjectConfiguration() }
        .map { config ->
            tokenizeModules(config)
        }

    override suspend fun getDeviceState(): DeviceState {
        val projectId = localDataSource.getProject().id
        val lastInstructionId = localDataSource.getDeviceConfiguration().lastInstructionId

        return remoteDataSource.getDeviceState(projectId, deviceId, lastInstructionId)
    }

    override suspend fun getDeviceConfiguration(): DeviceConfiguration = localDataSource.getDeviceConfiguration()

    override fun observeDeviceConfiguration(): Flow<DeviceConfiguration> = localDataSource.observeDeviceConfiguration()

    override suspend fun updateDeviceConfiguration(update: suspend (t: DeviceConfiguration) -> DeviceConfiguration) =
        localDataSource.updateDeviceConfiguration(update)

    override suspend fun clearData() {
        localDataSource.clearProject()
        localDataSource.clearProjectConfiguration()
        localDataSource.clearDeviceConfiguration()
        localDataSource.deletePrivacyNotices()
    }

    override fun getPrivacyNotice(
        projectId: String,
        language: String,
    ): Flow<PrivacyNoticeResult> = flow {
        if (localDataSource.hasPrivacyNoticeFor(projectId, language)) {
            val privacyNotice = localDataSource.getPrivacyNotice(projectId, language)
            emit(Succeed(language, privacyNotice))
        } else {
            downloadPrivacyNotice(this, projectId, language)
        }
    }

    private suspend fun tokenizeModules(config: ProjectConfiguration): ProjectConfiguration {
        // No need to handle NoSuchElementException, the configuration might get fetched while there is no project
        val project = runCatching { getProject() }.getOrNull() ?: return config
        return config.copy(
            synchronization = config.synchronization.copy(
                down = config.synchronization.down.copy(
                    simprints = config.synchronization.down.simprints?.copy(
                        moduleOptions = config.synchronization.down.simprints.moduleOptions.map { moduleId ->
                            tokenizationProcessor.tokenizeIfNecessary(
                                tokenizableString = moduleId,
                                tokenKeyType = TokenKeyType.ModuleId,
                                project = project,
                            )
                        },
                    ),
                ),
            ),
        )
    }

    private suspend fun downloadPrivacyNotice(
        flowCollector: FlowCollector<PrivacyNoticeResult>,
        projectId: String,
        language: String,
    ) {
        flowCollector.emit(InProgress(language))
        try {
            val privacyNotice =
                remoteDataSource.getPrivacyNotice(projectId, "${PRIVACY_NOTICE_FILE}_$language")
            localDataSource.storePrivacyNotice(projectId, language, privacyNotice)
            flowCollector.emit(Succeed(language, privacyNotice))
        } catch (t: Throwable) {
            if ((t.cause as? HttpException)?.code() != HttpURLConnection.HTTP_NOT_FOUND) {
                // Non-existence of resource isn't considered a download failure
                Simber.i("Failed to download privacy notice", t)
            }
            flowCollector.emit(
                if (t is BackendMaintenanceException) {
                    FailedBecauseBackendMaintenance(language, t, t.estimatedOutage)
                } else {
                    Failed(language, t)
                },
            )
        }
    }

    companion object {
        @VisibleForTesting
        const val PRIVACY_NOTICE_FILE = "privacy_notice"
    }
}
