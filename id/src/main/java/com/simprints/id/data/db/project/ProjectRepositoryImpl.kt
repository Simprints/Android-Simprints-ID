package com.simprints.id.data.db.project

import com.simprints.id.data.db.project.domain.Project
import com.simprints.id.data.db.project.local.ProjectLocalDataSource
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSource
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.infra.logging.PerformanceMonitor
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.exceptions.NetworkConnectionException

class ProjectRepositoryImpl(
    private val projectLocalDataSource: ProjectLocalDataSource,
    private val projectRemoteDataSource: ProjectRemoteDataSource,
    private val remoteConfigWrapper: RemoteConfigWrapper,
) : ProjectRepository {

    override suspend fun loadFromRemoteAndRefreshCache(projectId: String): Project? {
        val trace = PerformanceMonitor.trace("refreshProjectInfoWithServer").apply { start() }
        val projectInLocal = projectLocalDataSource.load(projectId)
        return projectInLocal?.also {
            fetchAndUpdateCache(it.id)
            trace.stop()
        } ?: fetchAndUpdateCache(projectId).apply {
            trace.stop()
        }
    }

    override suspend fun loadFromCache(projectId: String): Project? {
        return projectLocalDataSource.load(projectId)
    }

    override suspend fun fetchProjectConfigurationAndSave(projectId: String) {
        projectRemoteDataSource.loadProjectRemoteConfigSettingsJsonString(projectId).toString().let {
            remoteConfigWrapper.projectSettingsJsonString = it
        }
    }

    private suspend fun fetchAndUpdateCache(projectId: String): Project? = try {
        projectRemoteDataSource.loadProjectFromRemote(projectId).also {
            projectLocalDataSource.save(it)
        }
    } catch (t: Throwable) {
        when (t) {
            is NetworkConnectionException -> Simber.i(t)
            //TODO: should we ignore all other exceptions here
            else -> Simber.d(t)
        }
        null
    }

}
