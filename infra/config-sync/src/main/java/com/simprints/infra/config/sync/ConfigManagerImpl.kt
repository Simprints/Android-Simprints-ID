package com.simprints.infra.config.sync

import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.store.models.PrivacyNoticeResult
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.sync.worker.ConfigurationScheduler
import com.simprints.infra.enrolment.records.store.EnrolmentRecordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class ConfigManagerImpl @Inject constructor(
    private val configRepository: ConfigRepository,
    private val configurationScheduler: ConfigurationScheduler,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
) : ConfigManager {

    override suspend fun refreshProject(projectId: String): Pair<Project, ProjectConfiguration> =
        configRepository.refreshProject(projectId).also { (project, _) ->
            enrolmentRecordRepository.tokenizeExistingRecords(project)
        }

    override suspend fun getProject(projectId: String): Project =
        configRepository.getProject(projectId)

    override suspend fun getProjectConfiguration(): ProjectConfiguration =
        configRepository.getConfiguration()

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
        language: String,
    ): Flow<PrivacyNoticeResult> =
        configRepository.getPrivacyNotice(projectId, language)
}
