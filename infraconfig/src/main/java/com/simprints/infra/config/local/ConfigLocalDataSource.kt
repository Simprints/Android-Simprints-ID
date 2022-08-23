package com.simprints.infra.config.local

import com.simprints.infra.config.domain.models.DeviceConfiguration
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.domain.models.ProjectConfiguration

internal interface ConfigLocalDataSource {

    suspend fun saveProject(project: Project)

    suspend fun getProject(): Project

    suspend fun saveProjectConfiguration(config: ProjectConfiguration)

    suspend fun getProjectConfiguration(): ProjectConfiguration

    suspend fun getDeviceConfiguration(): DeviceConfiguration

    suspend fun updateDeviceConfiguration(update: suspend (t: DeviceConfiguration) -> DeviceConfiguration)
}
