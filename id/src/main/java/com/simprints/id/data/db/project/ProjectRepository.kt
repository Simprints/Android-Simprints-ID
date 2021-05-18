package com.simprints.id.data.db.project

import com.simprints.id.data.db.project.domain.Project
import com.simprints.id.data.db.project.local.ProjectLocalDataSource
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSource

interface ProjectRepository: ProjectLocalDataSource, ProjectRemoteDataSource {
    suspend fun loadFromRemoteAndRefreshCache(projectId: String): Project?
    suspend fun loadFromCache(projectId: String): Project?
}
