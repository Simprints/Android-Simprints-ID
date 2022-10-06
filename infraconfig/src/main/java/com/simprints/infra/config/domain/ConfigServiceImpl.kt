package com.simprints.infra.config.domain

import com.simprints.infra.config.domain.models.DeviceConfiguration
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.infra.config.local.ConfigLocalDataSource
import com.simprints.infra.config.remote.ConfigRemoteDataSource
import javax.inject.Inject

internal class ConfigServiceImpl @Inject constructor(
    private val localDataSource: ConfigLocalDataSource,
    private val remoteDataSource: ConfigRemoteDataSource
) : ConfigService {

    override suspend fun getProject(projectId: String): Project =
        try {
            localDataSource.getProject()
        } catch (e: Exception) {
            if (e is NoSuchElementException) {
                refreshProject(projectId)
            } else {
                throw e
            }
        }

    override suspend fun refreshProject(projectId: String): Project =
        remoteDataSource.getProject(projectId).also {
            localDataSource.saveProject(it)
        }

    override suspend fun getConfiguration(): ProjectConfiguration =
        localDataSource.getProjectConfiguration()

    override suspend fun refreshConfiguration(projectId: String): ProjectConfiguration =
        remoteDataSource.getConfiguration(projectId).also {
            localDataSource.saveProjectConfiguration(it)
        }

    override suspend fun getDeviceConfiguration(): DeviceConfiguration =
        localDataSource.getDeviceConfiguration()

    override suspend fun updateDeviceConfiguration(update: suspend (t: DeviceConfiguration) -> DeviceConfiguration) =
        localDataSource.updateDeviceConfiguration(update)

    override suspend fun clearData() {
        localDataSource.clearProject()
        localDataSource.clearProjectConfiguration()
        localDataSource.clearDeviceConfiguration()
    }
}
