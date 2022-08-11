package com.simprints.infra.config.domain

import com.simprints.infra.config.domain.models.Project
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
            }
            throw e
        }

    override suspend fun refreshProject(projectId: String): Project =
        remoteDataSource.getProject(projectId).also {
            localDataSource.saveProject(it)
        }
}
