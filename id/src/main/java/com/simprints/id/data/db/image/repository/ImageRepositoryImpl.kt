package com.simprints.id.data.db.image.repository

import com.simprints.core.images.Path
import com.simprints.core.images.SecuredImageRef
import com.simprints.id.data.db.image.local.ImageLocalDataSource
import com.simprints.id.data.db.image.remote.ImageRemoteDataSource
import com.simprints.id.data.db.image.remote.UploadResult

class ImageRepositoryImpl(
    private val localDataSource: ImageLocalDataSource,
    private val remoteDataSource: ImageRemoteDataSource
) : ImageRepository {

    override fun storeImageSecurely(
        imageBytes: ByteArray,
        relativePath: Path,
        fileName: String
    ): SecuredImageRef? {
        return localDataSource.encryptAndStoreImage(imageBytes, relativePath, fileName)
    }

    override suspend fun uploadStoredImagesAndDelete(): Boolean {
        val uploads = uploadImages()

        if (uploads.isEmpty())
            return true

        var allUploadsSuccessful = true

        uploads.forEach { upload ->
            if (upload.isSuccessful())
                localDataSource.deleteImage(upload.image)
            else
                allUploadsSuccessful = false
        }

        return allUploadsSuccessful
    }

    private suspend fun uploadImages(): List<UploadResult> {
        val images = localDataSource.listImages()
        return images.map {
            remoteDataSource.uploadImage(it)
        }
    }

}
