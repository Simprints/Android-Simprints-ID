package com.simprints.infra.config.sync

import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.store.models.PrivacyNoticeResult
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.ProjectWithConfig
import kotlinx.coroutines.flow.Flow

interface ConfigManager {
    /**
     * fetch the latest state of the project and save it locally
     */
    suspend fun refreshProject(projectId: String): ProjectWithConfig

    /**
     * get the project locally or if not present fetch it remotely
     */
    suspend fun getProject(projectId: String): Project

    /**
     * get the project configuration locally
     */
    suspend fun getProjectConfiguration(): ProjectConfiguration

    /**
     * fetch the current device configuration.
     */
    suspend fun getDeviceConfiguration(): DeviceConfiguration

    /**
     * update the device configuration
     */
    suspend fun updateDeviceConfiguration(update: suspend (t: DeviceConfiguration) -> DeviceConfiguration)

    /**
     * clears the project, project configuration and device configuration
     */
    suspend fun clearData()

    suspend fun getPrivacyNotice(projectId: String, language: String): Flow<PrivacyNoticeResult>

    fun scheduleSyncConfiguration()
    fun cancelScheduledSyncConfiguration()
}
