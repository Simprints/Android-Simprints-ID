package com.simprints.infra.config.store

import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.store.models.DeviceState
import com.simprints.infra.config.store.models.PrivacyNoticeResult
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.ProjectWithConfig
import kotlinx.coroutines.flow.Flow

interface ConfigRepository {
    suspend fun refreshProject(projectId: String): ProjectWithConfig

    suspend fun getProject(): Project

    suspend fun getProjectConfiguration(): ProjectConfiguration

    fun watchProjectConfiguration(): Flow<ProjectConfiguration>

    suspend fun getDeviceState(): DeviceState

    suspend fun getDeviceConfiguration(): DeviceConfiguration

    suspend fun updateDeviceConfiguration(update: suspend (t: DeviceConfiguration) -> DeviceConfiguration)

    suspend fun clearData()

    fun getPrivacyNotice(
        projectId: String,
        language: String,
    ): Flow<PrivacyNoticeResult>
}
