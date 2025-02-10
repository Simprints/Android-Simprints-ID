package com.simprints.infra.images

import com.simprints.infra.images.local.ImageLocalDataSource
import com.simprints.infra.images.metadata.ImageMetadataStore
import com.simprints.infra.images.model.Path
import com.simprints.infra.images.model.SecuredImageRef
import com.simprints.infra.images.remote.ImageRemoteDataSource
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.SYNC
import com.simprints.infra.logging.Simber
import javax.inject.Inject

internal class ImageRepositoryImpl @Inject internal constructor(
    private val localDataSource: ImageLocalDataSource,
    private val remoteDataSource: ImageRemoteDataSource,
    private val metadataStore: ImageMetadataStore,
) : ImageRepository {
    override suspend fun storeImageSecurely(
        imageBytes: ByteArray,
        projectId: String,
        relativePath: Path,
        metadata: Map<String, String>,
    ): SecuredImageRef? = localDataSource
        .encryptAndStoreImage(imageBytes, projectId, relativePath)
        // Only store metadata if the image was stored successfully
        ?.also { metadataStore.storeMetadata(relativePath, metadata) }

    override suspend fun getNumberOfImagesToUpload(projectId: String): Int = localDataSource.listImages(projectId).count()

    override suspend fun uploadStoredImagesAndDelete(projectId: String): Boolean {
        var allImagesUploaded = true

        val images = localDataSource.listImages(projectId)
        images.forEach { imageRef ->
            try {
                localDataSource.decryptImage(imageRef)?.let { stream ->
                    val metadata = metadataStore.getMetadata(imageRef.relativePath)
                    val uploadResult = remoteDataSource.uploadImage(stream, imageRef, metadata)
                    if (uploadResult.isUploadSuccessful()) {
                        localDataSource.deleteImage(imageRef)
                        metadataStore.deleteMetadata(imageRef.relativePath)
                    } else {
                        allImagesUploaded = false
                        Simber.i("Failed to upload image without exception", tag = SYNC)
                    }
                }
            } catch (t: Throwable) {
                allImagesUploaded = false
                Simber.e("Failed to upload images", t, tag = SYNC)
            }
        }

        return allImagesUploaded
    }

    override suspend fun deleteStoredImages() {
        metadataStore.deleteAllMetadata()
        for (image in localDataSource.listImages(null)) {
            localDataSource.deleteImage(image)
        }
    }
}
