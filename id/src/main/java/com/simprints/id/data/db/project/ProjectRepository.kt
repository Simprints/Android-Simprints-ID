package com.simprints.eventsystem.project

import com.simprints.eventsystem.project.domain.Project
import com.simprints.eventsystem.project.local.ProjectLocalDataSource
import com.simprints.eventsystem.project.remote.ProjectRemoteDataSource

interface ProjectRepository: ProjectLocalDataSource, ProjectRemoteDataSource {
    suspend fun loadFromRemoteAndRefreshCache(projectId: String): Project?
    suspend fun loadFromCache(projectId: String): Project?
}
