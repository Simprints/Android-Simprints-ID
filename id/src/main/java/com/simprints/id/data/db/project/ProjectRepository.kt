package com.simprints.id.data.db.project

import com.simprints.id.data.db.project.domain.Project

interface ProjectRepository {
    suspend fun loadFromRemoteAndRefreshCache(projectId: String): Project?
    suspend fun loadFromCache(projectId: String): Project?
    suspend fun fetchProjectConfigurationAndSave(projectId: String)
}
