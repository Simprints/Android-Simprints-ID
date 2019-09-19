package com.simprints.id.data.db.project.local

import com.simprints.id.data.db.project.domain.Project

interface ProjectLocalDataSource {

    suspend fun save(project: Project)

    suspend fun load(projectId: String): Project?
}
