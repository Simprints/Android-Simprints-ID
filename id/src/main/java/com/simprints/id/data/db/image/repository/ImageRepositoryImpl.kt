package com.simprints.id.data.db.image.repository

import com.simprints.core.images.SecuredImageRef
import com.simprints.id.data.db.image.local.ImageLocalDataSource
import com.simprints.id.data.db.image.remote.ImageRemoteDataSource
import com.simprints.id.data.db.image.remote.UploadResult

class ImageRepositoryImpl(
    private val localDataSource: ImageLocalDataSource,
    private val remoteDataSource: ImageRemoteDataSource
) : ImageRepository {

    override suspend fun deleteImage(image: SecuredImageRef): Boolean {
        return localDataSource.deleteImage(image)
    }

    override suspend fun uploadImages(): List<UploadResult> {
        val images = localDataSource.listImages()
        return images.map {
            remoteDataSource.uploadImage(it)
        }
    }

}
