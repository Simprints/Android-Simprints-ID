package com.simprints.infra.config.domain

import com.simprints.infra.config.domain.models.Project

interface ConfigService {

    suspend fun refreshProject(projectId: String): Project

    suspend fun getProject(projectId: String): Project
}
