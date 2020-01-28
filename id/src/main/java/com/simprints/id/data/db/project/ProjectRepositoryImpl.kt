package com.simprints.id.data.db.project

import com.google.firebase.perf.FirebasePerformance
import com.simprints.id.data.db.project.domain.Project
import com.simprints.id.data.db.project.local.ProjectLocalDataSource
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
        return projectInLocal?.apply {
            fetchAndUpdateCache(projectId)
            trace.stop()
        } ?: fetchAndUpdateCache(projectId).apply {
            trace.stop()
        }
    }

    override suspend fun loadFromCache(projectId: String): Project? {
        return projectLocalDataSource.load(projectId)
    }

    private suspend fun fetchAndUpdateCache(projectId: String): Project? = try {
        withContext(Dispatchers.IO) {
            projectRemoteDataSource.loadProjectFromRemote(projectId).blockingGet().also {
                projectLocalDataSource.save(it)
            }
        }
    } catch (t: Throwable) {
        t.printStackTrace()
        null
    }
}
