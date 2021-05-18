package com.simprints.eventsystem.project.local

import com.simprints.eventsystem.project.domain.Project

interface ProjectLocalDataSource {

    suspend fun save(project: Project)

    suspend fun load(projectId: String): Project?
}
