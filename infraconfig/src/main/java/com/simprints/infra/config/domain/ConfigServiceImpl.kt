package com.simprints.infra.config.domain

import androidx.annotation.VisibleForTesting
import com.simprints.infra.config.domain.models.DeviceConfiguration
import com.simprints.infra.config.domain.models.PrivacyNoticeResult
import com.simprints.infra.config.domain.models.PrivacyNoticeResult.*
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.infra.config.local.ConfigLocalDataSource
import com.simprints.infra.config.remote.ConfigRemoteDataSource
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

internal class ConfigServiceImpl @Inject constructor(
    private val localDataSource: ConfigLocalDataSource,
    private val remoteDataSource: ConfigRemoteDataSource,
) : ConfigService {

    companion object {
        @VisibleForTesting
        const val PRIVACY_NOTICE_FILE = "privacy_notice"
    }

    override suspend fun getProject(projectId: String): Project = try {
        localDataSource.getProject()
    } catch (e: Exception) {
        if (e is NoSuchElementException) {
            refreshProject(projectId)
        } else {
            throw e
        }
    }

    override suspend fun refreshProject(projectId: String): Project = remoteDataSource
        .getProject(projectId)
        .also { localDataSource.saveProject(it) }

    override suspend fun getConfiguration(): ProjectConfiguration = localDataSource.getProjectConfiguration()

    override suspend fun refreshConfiguration(projectId: String): ProjectConfiguration = remoteDataSource
        .getConfiguration(projectId)
        .also { localDataSource.saveProjectConfiguration(it) }

    override suspend fun getDeviceConfiguration(): DeviceConfiguration = localDataSource.getDeviceConfiguration()

    override suspend fun updateDeviceConfiguration(
        update: suspend (t: DeviceConfiguration) -> DeviceConfiguration
    ) = localDataSource.updateDeviceConfiguration(update)

    override suspend fun clearData() {
        localDataSource.clearProject()
        localDataSource.clearProjectConfiguration()
        localDataSource.clearDeviceConfiguration()
        localDataSource.deletePrivacyNotices()
    }

    override suspend fun getPrivacyNotice(
        projectId: String,
        language: String
    ): Flow<PrivacyNoticeResult> =
        flow {
            if (localDataSource.hasPrivacyNoticeFor(projectId, language)) {
                val privacyNotice = localDataSource.getPrivacyNotice(projectId, language)
                emit(Succeed(language, privacyNotice))
            } else {
                downloadPrivacyNotice(this, projectId, language)
            }
        }

    private suspend fun downloadPrivacyNotice(
        flowCollector: FlowCollector<PrivacyNoticeResult>,
        projectId: String,
        language: String
    ) {
        flowCollector.emit(InProgress(language))
        try {
            val privacyNotice =
                remoteDataSource.getPrivacyNotice(projectId, "${PRIVACY_NOTICE_FILE}_$language")
            localDataSource.storePrivacyNotice(projectId, language, privacyNotice)
            flowCollector.emit(Succeed(language, privacyNotice))
        } catch (t: Throwable) {
            Simber.i(t)
            flowCollector.emit(
                if (t is BackendMaintenanceException)
                    FailedBecauseBackendMaintenance(language, t, t.estimatedOutage)
                else
                    Failed(language, t)
            )
        }
    }
}
