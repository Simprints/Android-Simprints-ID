package com.simprints.infra.config.store.remote

import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectConfiguration

internal interface ConfigRemoteDataSource {

    suspend fun getProject(projectId: String): Pair<Project, ProjectConfiguration>

    suspend fun getPrivacyNotice(projectId: String, fileId: String): String
}
