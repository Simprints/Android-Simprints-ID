package com.simprints.id.network

import com.simprints.infra.login.domain.LoginInfoManager
import com.simprints.id.data.db.project.local.ProjectLocalDataSource

// TODO move this into infraconfig
class ImageUrlProviderImpl(
    private val projectLocalDataSource: ProjectLocalDataSource,
    private val loginInfoManager: LoginInfoManager
) : ImageUrlProvider {

    override suspend fun getImageStorageBucketUrl(): String? {
        val projectId = loginInfoManager.getSignedInProjectIdOrEmpty()

        if (projectId.isEmpty())
            return null

        val imageStorageBucketUrl = projectLocalDataSource.load(projectId)?.imageBucket

        return if (imageStorageBucketUrl.isNullOrEmpty()) {
            "gs://$projectId-images-eu"
        } else {
            imageStorageBucketUrl
        }
    }

}
