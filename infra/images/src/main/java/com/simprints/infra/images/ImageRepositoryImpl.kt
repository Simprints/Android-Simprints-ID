package com.simprints.infra.images

import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.images.local.ImageLocalDataSource
import com.simprints.infra.images.metadata.ImageMetadataStore
import com.simprints.infra.images.model.SecuredImageRef
import com.simprints.infra.images.remote.SampleUploader
import com.simprints.infra.images.usecase.CreateSamplePathUseCase
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.FACE_CAPTURE
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.FINGER_CAPTURE
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.SYNC
import com.simprints.infra.logging.Simber
import javax.inject.Inject

internal class ImageRepositoryImpl @Inject internal constructor(
    private val localDataSource: ImageLocalDataSource,
    private val sampleUploader: SampleUploader,
    private val metadataStore: ImageMetadataStore,
    private val createSamplePathUseCase: CreateSamplePathUseCase,
) : ImageRepository {
    override suspend fun storeSample(
        projectId: String,
        sessionId: String,
        modality: GeneralConfiguration.Modality,
        sampleId: String,
        fileExtension: String,
        sampleBytes: ByteArray,
        metadata: Map<String, String>,
    ): SecuredImageRef? {
        val logTag = if (modality == GeneralConfiguration.Modality.FACE) FACE_CAPTURE else FINGER_CAPTURE

        val relativePath = try {
            createSamplePathUseCase(sessionId, modality, sampleId, fileExtension)
        } catch (t: Throwable) {
            Simber.e("Error determining path for $sessionId", t, tag = logTag)
            return null
        }

        Simber.d("Saving $modality sample to ${relativePath.compose()}", tag = logTag)

        return localDataSource
            .encryptAndStoreImage(sampleBytes, projectId, relativePath)
            .also {
                if (it == null) {
                    Simber.i("Saving image failed for captureId $sessionId", tag = logTag)
                } else {
                    // Only store metadata if the image was stored successfully
                    metadataStore.storeMetadata(relativePath, metadata)
                }
            }
    }

    override suspend fun getNumberOfImagesToUpload(projectId: String): Int = localDataSource.listImages(projectId).count()

    override suspend fun uploadStoredImagesAndDelete(projectId: String): Boolean {
        var allImagesUploaded = true

        val images = localDataSource.listImages(projectId)
        images.forEach { imageRef ->
            try {
                localDataSource.decryptImage(imageRef)?.let { stream ->
                    val metadata = metadataStore.getMetadata(imageRef.relativePath)
                    val uploadResult = sampleUploader.uploadSample(stream, imageRef, metadata)
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
