package com.simprints.infra.config.local

import com.simprints.infra.config.domain.models.Project

internal interface ConfigLocalDataSource {

    suspend fun saveProject(project: Project)

    suspend fun getProject(): Project
}
