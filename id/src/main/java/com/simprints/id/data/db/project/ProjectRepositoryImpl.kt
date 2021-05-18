package com.simprints.eventsystem.project

import com.google.firebase.perf.FirebasePerformance
import com.simprints.eventsystem.project.domain.Project
import com.simprints.eventsystem.project.local.ProjectLocalDataSource
import com.simprints.eventsystem.project.remote.ProjectRemoteDataSource

class ProjectRepositoryImpl(
    private val projectLocalDataSource: ProjectLocalDataSource,
    private val projectRemoteDataSource: ProjectRemoteDataSource,
    private val performanceTracker: FirebasePerformance = FirebasePerformance.getInstance()
) : ProjectRepository,
    ProjectLocalDataSource by projectLocalDataSource,
    ProjectRemoteDataSource by projectRemoteDataSource {

    override suspend fun loadFromRemoteAndRefreshCache(projectId: String): Project? {
        val trace = performanceTracker.newTrace("refreshProjectInfoWithServer").apply { start() }
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

    private suspend fun fetchAndUpdateCache(projectId: String): Project? = try {
        projectRemoteDataSource.loadProjectFromRemote(projectId).also {
            projectLocalDataSource.save(it)
        }
    } catch (t: Throwable) {
        t.printStackTrace()
        null
    }

}
