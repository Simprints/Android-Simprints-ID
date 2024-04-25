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
        metadata: Map<String, String>,
    ): SecuredImageRef? {
        // TODO store metadata

        return localDataSource.encryptAndStoreImage(imageBytes, projectId, relativePath)
    }

    override suspend fun getNumberOfImagesToUpload(projectId: String): Int =
        localDataSource.listImages(projectId).count()


    override suspend fun uploadStoredImagesAndDelete(projectId: String): Boolean {
        var allImagesUploaded = true

        val images = localDataSource.listImages(projectId)
        images.forEach { imageRef ->
            try {
                localDataSource.decryptImage(imageRef)?.let { stream ->
                    val metadata = emptyMap<String, String>() // TODO fetch metadata

                    val uploadResult = remoteDataSource.uploadImage(stream, imageRef, metadata)
                    if (uploadResult.isUploadSuccessful()) {
                        localDataSource.deleteImage(imageRef)
                    } else {
                        allImagesUploaded = false
                        Simber.e("Failed to upload image without exception")
                    }
                }
            } catch (t: Throwable) {
                allImagesUploaded = false
                Simber.e(t)
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
