package com.simprints.infra.images

import com.simprints.infra.images.local.ImageLocalDataSource
import com.simprints.infra.images.model.Path
import com.simprints.infra.images.model.SecuredImageRef
import com.simprints.infra.images.remote.ImageRemoteDataSource
import com.simprints.infra.logging.Simber
import javax.inject.Inject

class ImageRepositoryImpl @Inject internal constructor(
    private val localDataSource: ImageLocalDataSource,
    private val remoteDataSource: ImageRemoteDataSource
) : ImageRepository {

    override fun storeImageSecurely(imageBytes: ByteArray, relativePath: Path): SecuredImageRef? {
        return localDataSource.encryptAndStoreImage(imageBytes, relativePath)
    }

    override fun getNumberOfImagesToUpload(): Int = localDataSource.listImages().count()

    override suspend fun uploadStoredImagesAndDelete(): Boolean {
        var allImagesUploaded = true

        val images = localDataSource.listImages()
        images.forEach { imageRef ->
            try {
                localDataSource.decryptImage(imageRef)?.let { stream ->
                    val uploadResult = remoteDataSource.uploadImage(stream, imageRef)
                    if (uploadResult.isUploadSuccessful()) {
                        localDataSource.deleteImage(imageRef)
                    } else {
                        allImagesUploaded = false
                    }
                }
            } catch (t: Throwable) {
                allImagesUploaded = false
                Simber.d(t)
            }
        }

        return allImagesUploaded
    }

    override fun deleteStoredImages() = with(localDataSource) {
        listImages().forEach {
            deleteImage(it)
        }
    }
}
