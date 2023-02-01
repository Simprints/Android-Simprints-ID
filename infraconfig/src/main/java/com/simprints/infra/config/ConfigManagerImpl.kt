package com.simprints.infra.config

import com.simprints.infra.config.domain.ConfigService
import com.simprints.infra.config.domain.models.DeviceConfiguration
import com.simprints.infra.config.domain.models.PrivacyNoticeResult
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.infra.config.worker.ConfigurationScheduler
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class ConfigManagerImpl @Inject constructor(
    private val configRepository: ConfigService,
    private val configurationScheduler: ConfigurationScheduler
) :
    ConfigManager {

    override suspend fun refreshProject(projectId: String): Project =
        configRepository.refreshProject(projectId)

    override suspend fun getProject(projectId: String): Project =
        configRepository.getProject(projectId)

    override suspend fun getProjectConfiguration(): ProjectConfiguration =
        configRepository.getConfiguration()

    override suspend fun refreshProjectConfiguration(projectId: String): ProjectConfiguration =
        configRepository.refreshConfiguration(projectId)

    override suspend fun getDeviceConfiguration(): DeviceConfiguration =
        configRepository.getDeviceConfiguration()

    override suspend fun updateDeviceConfiguration(update: suspend (t: DeviceConfiguration) -> DeviceConfiguration) =
        configRepository.updateDeviceConfiguration(update)

    override fun scheduleSyncConfiguration() =
        configurationScheduler.scheduleSync()

    override fun cancelScheduledSyncConfiguration() =
        configurationScheduler.cancelScheduledSync()

    override suspend fun clearData() =
        configRepository.clearData()

    override suspend fun getPrivacyNotice(
        projectId: String,
        language: String
    ): Flow<PrivacyNoticeResult> =
        configRepository.getPrivacyNotice(projectId, language)
}
