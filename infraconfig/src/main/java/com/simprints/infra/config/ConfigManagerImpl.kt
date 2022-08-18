package com.simprints.infra.config

import com.simprints.infra.config.domain.ConfigService
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.infra.config.worker.ConfigurationScheduler
import kotlinx.coroutines.runBlocking
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

    override suspend fun getConfiguration(): ProjectConfiguration =
        configRepository.getConfiguration()

    override suspend fun refreshConfiguration(projectId: String): ProjectConfiguration =
        configRepository.refreshConfiguration(projectId)

    override fun scheduleSyncConfiguration() =
        configurationScheduler.scheduleSync()

    override fun cancelScheduledSyncConfiguration() =
        configurationScheduler.cancelScheduledSync()

}
