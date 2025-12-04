package com.simprints.infra.config.sync

import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.store.models.DeviceState
import com.simprints.infra.config.store.models.PrivacyNoticeResult
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.ProjectWithConfig
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigManager @Inject constructor(
    private val configRepository: ConfigRepository,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val configSyncCache: ConfigSyncCache,
    private val authStore: AuthStore,
) {
    private val isProjectRefreshingFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    suspend fun refreshProject(projectId: String): ProjectWithConfig {
        isProjectRefreshingFlow.tryEmit(true)
        try {
            return configRepository.refreshProject(projectId).also {
                enrolmentRecordRepository.tokenizeExistingRecords(it.project)
                configSyncCache.saveUpdateTime()
            }
        } finally {
            isProjectRefreshingFlow.tryEmit(false)
        }
    }

    suspend fun getProject(): Project? = try {
        configRepository.getProject()
    } catch (_: NoSuchElementException) {
        val projectId = authStore.signedInProjectId
        if (projectId.isEmpty()) {
            null
        } else {
            try {
                refreshProject(projectId).project
            } catch (_: Exception) {
                null
            }
        }
    }

    suspend fun getProjectConfiguration(): ProjectConfiguration {
        val localConfig = configRepository.getProjectConfiguration()
        // If projectId is empty, configuration hasn't been downloaded yet
        return if (localConfig.projectId.isEmpty()) {
            val signedProjectId = authStore.signedInProjectId.takeUnless { it.isEmpty() } ?: return localConfig

            try {
                // Try to refresh it with logged in projectId (if any)
                refreshProject(signedProjectId).configuration
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

    fun observeIsProjectRefreshing(): Flow<Boolean> = isProjectRefreshingFlow.asStateFlow()

    fun observeProjectConfiguration(): Flow<ProjectConfiguration> = configRepository
        .observeProjectConfiguration()
        .onStart { getProjectConfiguration() } // to invoke download if empty

    suspend fun getDeviceConfiguration(): DeviceConfiguration = configRepository.getDeviceConfiguration()

    fun observeDeviceConfiguration(): Flow<DeviceConfiguration> = configRepository
        .observeDeviceConfiguration()
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
