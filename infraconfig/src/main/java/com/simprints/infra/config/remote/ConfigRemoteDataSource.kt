package com.simprints.infra.config.remote

import com.simprints.infra.config.domain.Project
import com.simprints.infra.config.domain.ProjectConfiguration

interface ConfigRemoteDataSource {

    suspend fun getConfiguration(projectId: String): ProjectConfiguration

    suspend fun getProject(projectId: String): Project
}
