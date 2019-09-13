package com.simprints.id.data.db.project

import com.google.firebase.perf.FirebasePerformance
import com.simprints.id.data.db.project.domain.Project
import com.simprints.id.data.db.project.local.ProjectLocalDataSource
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ProjectRepositoryImpl(private val projectLocalDataSource: ProjectLocalDataSource,
                            private val projectRemoteDataSource: ProjectRemoteDataSource,
                            private val performanceTracker: FirebasePerformance = FirebasePerformance.getInstance()) :
    ProjectRepository,
    ProjectLocalDataSource by projectLocalDataSource,
    ProjectRemoteDataSource by projectRemoteDataSource {

    override suspend fun loadAndRefreshCache(projectId: String): Project {
        val trace = performanceTracker.newTrace("refreshProjectInfoWithServer").apply { start() }
        return projectLocalDataSource.load(projectId).also {
            GlobalScope.launch(Dispatchers.IO) {
                refreshCache(projectId)
                trace.stop()
            }
        }
    }

    private suspend fun refreshCache(projectId: String) {
        val project = projectRemoteDataSource.loadProjectFromRemote(projectId).blockingGet() //TODO: transform remote in coroutines
        projectLocalDataSource.save(project)
    }
}
