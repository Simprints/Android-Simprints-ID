package com.simprints.infra.config.sync

import com.simprints.infra.config.store.ConfigService
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.store.models.PrivacyNoticeResult
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.sync.worker.ConfigurationScheduler
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class ConfigManagerImpl @Inject constructor(
    private val configService: ConfigService,
    private val configurationScheduler: ConfigurationScheduler
) :
    ConfigManager {

    override suspend fun refreshProject(projectId: String): Project =
        configService.refreshProject(projectId)

    override suspend fun getProject(projectId: String): Project =
        configService.getProject(projectId)

    override suspend fun getProjectConfiguration(): ProjectConfiguration =
        configService.getConfiguration()

    override suspend fun refreshProjectConfiguration(projectId: String): ProjectConfiguration =
        configService.refreshConfiguration(projectId)

    override suspend fun getDeviceConfiguration(): DeviceConfiguration =
        configService.getDeviceConfiguration()

    override suspend fun updateDeviceConfiguration(update: suspend (t: DeviceConfiguration) -> DeviceConfiguration) =
        configService.updateDeviceConfiguration(update)

    override fun scheduleSyncConfiguration() =
        configurationScheduler.scheduleSync()

    override fun cancelScheduledSyncConfiguration() =
        configurationScheduler.cancelScheduledSync()

    override suspend fun clearData() =
        configService.clearData()

    override suspend fun getPrivacyNotice(
        projectId: String,
        language: String
    ): Flow<PrivacyNoticeResult> =
        configService.getPrivacyNotice(projectId, language)
}
