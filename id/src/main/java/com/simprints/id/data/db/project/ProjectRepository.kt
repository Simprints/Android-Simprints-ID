package com.simprints.id.data.db.project

import com.simprints.id.data.db.project.domain.Project

interface ProjectRepository {

    suspend fun loadAndRefreshCache(projectId: String): Project
}
