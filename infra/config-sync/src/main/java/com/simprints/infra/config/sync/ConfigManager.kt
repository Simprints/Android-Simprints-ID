package com.simprints.infra.config.sync

import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.store.models.DeviceState
import com.simprints.infra.config.store.models.PrivacyNoticeResult
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.ProjectWithConfig
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.local.migration.RealmToRoomMigrationScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class ConfigManager @Inject constructor(
    private val configRepository: ConfigRepository,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val configSyncCache: ConfigSyncCache,
    private val realmToRoomMigrationScheduler: RealmToRoomMigrationScheduler,
) {
    private val ifProjectRefreshingFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    suspend fun refreshProject(projectId: String): ProjectWithConfig {
        ifProjectRefreshingFlow.tryEmit(true)
        try {
            return configRepository.refreshProject(projectId).also {
                enrolmentRecordRepository.tokenizeExistingRecords(it.project)
                configSyncCache.saveUpdateTime()
                realmToRoomMigrationScheduler.scheduleMigrationWorkerIfNeeded()
            }
        } finally {
            ifProjectRefreshingFlow.tryEmit(false)
        }
    }

    suspend fun getProject(projectId: String): Project = try {
        configRepository.getProject()
    } catch (_: NoSuchElementException) {
        refreshProject(projectId).project
    }

    suspend fun getProjectConfiguration(): ProjectConfiguration {
        val localConfig = configRepository.getProjectConfiguration()
        // If projectId is empty, configuration hasn't been downloaded yet
        return if (localConfig.projectId.isEmpty()) {
            try {
                // Try to refresh it with logged in projectId (if any)
                refreshProject(configRepository.getProject().id).configuration
            } catch (_: Exception) {
                // If not logged in the above will fail. However we still depend on the 'default'
                // configuration to create the session when login is attempted. Possibly in other
                // places, too.
                localConfig
            }
        } else {
            localConfig
        }
    }

    fun watchIfProjectRefreshing(): Flow<Boolean> = ifProjectRefreshingFlow.asStateFlow()

    fun watchProjectConfiguration(): Flow<ProjectConfiguration> = configRepository
        .watchProjectConfiguration()
        .onStart { getProjectConfiguration() } // to invoke download if empty

    suspend fun getDeviceConfiguration(): DeviceConfiguration = configRepository.getDeviceConfiguration()

    fun watchDeviceConfiguration(): Flow<DeviceConfiguration> = configRepository
        .watchDeviceConfiguration()
        .onStart { getDeviceConfiguration() }

    suspend fun updateDeviceConfiguration(update: suspend (t: DeviceConfiguration) -> DeviceConfiguration) =
        configRepository.updateDeviceConfiguration(update)

    fun getPrivacyNotice(
        projectId: String,
        language: String,
    ): Flow<PrivacyNoticeResult> = configRepository.getPrivacyNotice(
        projectId = projectId,
        language = language,
    )

    suspend fun clearData() = configRepository.clearData()

    suspend fun getDeviceState(): DeviceState = configRepository.getDeviceState()
}
