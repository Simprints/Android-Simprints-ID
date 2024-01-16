package com.simprints.infra.config.store.remote

import com.simprints.infra.config.store.models.ProjectWithConfig

internal interface ConfigRemoteDataSource {

    suspend fun getProject(projectId: String): ProjectWithConfig

    suspend fun getPrivacyNotice(projectId: String, fileId: String): String
}
