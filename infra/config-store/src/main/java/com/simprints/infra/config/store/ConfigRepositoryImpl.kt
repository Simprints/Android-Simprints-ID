package com.simprints.infra.config.store

import androidx.annotation.VisibleForTesting
import com.simprints.core.DeviceID
import com.simprints.core.domain.tokenization.TokenizableString
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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class ConfigRepositoryImpl @Inject constructor(
    private val localDataSource: ConfigLocalDataSource,
    private val remoteDataSource: ConfigRemoteDataSource,
    private val simNetwork: SimNetwork,
    private val tokenizationProcessor: TokenizationProcessor,
    @DeviceID private val deviceId: String,
) : ConfigRepository {
    companion object {
        @VisibleForTesting
        const val PRIVACY_NOTICE_FILE = "privacy_notice"
    }

    override suspend fun getProject(): Project = localDataSource.getProject()

    override suspend fun refreshProject(projectId: String): ProjectWithConfig = remoteDataSource
        .getProject(projectId)
        .also { (project, configuration) ->
            localDataSource.saveProject(project)
            localDataSource.saveProjectConfiguration(configuration)

            if (!project.baseUrl.isNullOrBlank()) {
                simNetwork.setApiBaseUrl(project.baseUrl)
            }
        }

    override suspend fun getProjectConfiguration(): ProjectConfiguration {
        val config = localDataSource.getProjectConfiguration()
        return tokenizeModules(config)
    }

    override fun watchProjectConfiguration(): Flow<ProjectConfiguration> = localDataSource.watchProjectConfiguration().map { config ->
        tokenizeModules(config)
    }

    override suspend fun getDeviceState(): DeviceState {
        val projectId = localDataSource.getProject().id
        val lastInstructionId = localDataSource.getDeviceConfiguration().lastInstructionId

        return remoteDataSource.getDeviceState(projectId, deviceId, lastInstructionId)
    }

    override suspend fun getDeviceConfiguration(): DeviceConfiguration = localDataSource.getDeviceConfiguration()

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
                    moduleOptions = config.synchronization.down.moduleOptions.map { moduleId ->
                        when (moduleId) {
                            is TokenizableString.Raw -> tokenizationProcessor.encrypt(
                                decrypted = moduleId,
                                tokenKeyType = TokenKeyType.ModuleId,
                                project = project,
                            )

                            is TokenizableString.Tokenized -> moduleId
                        }
                    },
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
            Simber.i("Failed to download privacy notice", t)
            flowCollector.emit(
                if (t is BackendMaintenanceException) {
                    FailedBecauseBackendMaintenance(language, t, t.estimatedOutage)
                } else {
                    Failed(language, t)
                },
            )
        }
    }
}
