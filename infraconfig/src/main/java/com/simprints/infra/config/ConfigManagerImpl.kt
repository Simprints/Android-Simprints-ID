package com.simprints.infra.config

import com.simprints.infra.config.domain.ConfigService
import com.simprints.infra.config.domain.models.Project
import javax.inject.Inject

internal class ConfigManagerImpl @Inject constructor(private val configRepository: ConfigService) :
    ConfigManager {

    override suspend fun refreshProject(projectId: String): Project =
        configRepository.refreshProject(projectId)

    override suspend fun getProject(projectId: String): Project =
        configRepository.getProject(projectId)
}
