package com.simprints.infra.images

import com.simprints.infra.images.local.ImageLocalDataSource
import com.simprints.infra.images.model.Path
import com.simprints.infra.images.model.SecuredImageRef
import com.simprints.infra.images.remote.ImageRemoteDataSource
import com.simprints.infra.logging.Simber
import javax.inject.Inject

class ImageRepositoryImpl @Inject internal constructor(
    private val localDataSource: ImageLocalDataSource,
    private val remoteDataSource: ImageRemoteDataSource,
) : ImageRepository {

    override suspend fun storeImageSecurely(
        imageBytes: ByteArray,
        projectId: String,
        relativePath: Path,
    ): SecuredImageRef? = localDataSource.encryptAndStoreImage(imageBytes, projectId, relativePath)

    override suspend fun getNumberOfImagesToUpload(projectId: String): Int =
        localDataSource.listImages(projectId).count()


    override suspend fun uploadStoredImagesAndDelete(projectId: String): Boolean {
        var allImagesUploaded = true

        val images = localDataSource.listImages(projectId)
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

    override suspend fun deleteStoredImages() {
        for (image in localDataSource.listImages(null)) {
            localDataSource.deleteImage(image)
        }
    }
}
