package com.simprints.id.network

import com.simprints.id.data.db.project.local.ProjectLocalDataSource
import com.simprints.infra.login.LoginManager

// TODO move this into infraconfig
class ImageUrlProviderImpl(
    private val projectLocalDataSource: ProjectLocalDataSource,
    private val loginManager: LoginManager
) : ImageUrlProvider {

    override suspend fun getImageStorageBucketUrl(): String? {
        val projectId = loginManager.getSignedInProjectIdOrEmpty()

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
