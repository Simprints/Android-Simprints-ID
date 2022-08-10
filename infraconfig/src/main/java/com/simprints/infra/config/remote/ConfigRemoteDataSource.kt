package com.simprints.infra.config.remote

import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.domain.models.ProjectConfiguration

internal interface ConfigRemoteDataSource {

    suspend fun getConfiguration(projectId: String): ProjectConfiguration

    suspend fun getProject(projectId: String): Project
}
