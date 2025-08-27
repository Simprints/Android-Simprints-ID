package com.simprints.infra.config.store.local

import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectConfiguration
import kotlinx.coroutines.flow.Flow

internal interface ConfigLocalDataSource {
    suspend fun saveProject(project: Project)

    suspend fun getProject(): Project

    suspend fun clearProject()

    suspend fun saveProjectConfiguration(config: ProjectConfiguration)

    suspend fun getProjectConfiguration(): ProjectConfiguration

    fun observeProjectConfiguration(): Flow<ProjectConfiguration>

    suspend fun clearProjectConfiguration()

    suspend fun getDeviceConfiguration(): DeviceConfiguration

    fun observeDeviceConfiguration(): Flow<DeviceConfiguration>

    suspend fun updateDeviceConfiguration(update: suspend (t: DeviceConfiguration) -> DeviceConfiguration)

    suspend fun clearDeviceConfiguration()

    fun hasPrivacyNoticeFor(
        projectId: String,
        language: String,
    ): Boolean

    fun storePrivacyNotice(
        projectId: String,
        language: String,
        content: String,
    )

    fun getPrivacyNotice(
        projectId: String,
        language: String,
    ): String

    fun deletePrivacyNotices()
}
