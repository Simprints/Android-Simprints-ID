package com.simprints.infra.config.sync

import com.simprints.infra.config.store.ConfigService
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.store.models.PrivacyNoticeResult
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.sync.worker.ConfigurationScheduler
import com.simprints.infra.events.EventRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class ConfigManagerImpl @Inject constructor(
    private val configRepository: ConfigService,
    private val eventRepository: EventRepository,
    private val configurationScheduler: ConfigurationScheduler
) :
    ConfigManager {

    override suspend fun refreshProject(projectId: String): Project =
        configRepository.refreshProject(projectId).also { eventRepository.tokenizeLocalEvents(it) }

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
