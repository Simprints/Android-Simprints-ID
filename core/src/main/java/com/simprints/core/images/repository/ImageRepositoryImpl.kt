package com.simprints.core.images.repository

import android.content.Context
import com.simprints.core.images.local.ImageLocalDataSource
import com.simprints.core.images.local.ImageLocalDataSourceImpl
import com.simprints.core.images.model.Path
import com.simprints.core.images.model.SecuredImageRef
import com.simprints.core.images.remote.ImageRemoteDataSource
import com.simprints.core.images.remote.ImageRemoteDataSourceImpl
import com.simprints.core.images.remote.UploadResult

class ImageRepositoryImpl internal constructor(
    private val localDataSource: ImageLocalDataSource,
    private val remoteDataSource: ImageRemoteDataSource
) : ImageRepository {

    constructor(context: Context) : this(
        ImageLocalDataSourceImpl(context),
        ImageRemoteDataSourceImpl()
    )

    override fun storeImageSecurely(imageBytes: ByteArray, relativePath: Path): SecuredImageRef? {
        return localDataSource.encryptAndStoreImage(imageBytes, relativePath)
    }

    override suspend fun uploadStoredImagesAndDelete(): Boolean {
        val uploads = uploadImages()
        return getOperationResult(uploads)
    }

    private suspend fun uploadImages(): List<UploadResult> {
        val images = localDataSource.listImages()
        return images.map { imageRef ->
            localDataSource.decryptImage(imageRef)?.let { stream ->
                remoteDataSource.uploadImage(stream, imageRef)
            } ?: UploadResult(
                imageRef,
                UploadResult.Status.FAILED
            )
        }
    }

    private fun getOperationResult(uploads: List<UploadResult>): Boolean {
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

}
