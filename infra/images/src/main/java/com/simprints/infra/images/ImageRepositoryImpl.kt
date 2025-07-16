package com.simprints.infra.images

import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.images.local.ImageLocalDataSource
import com.simprints.infra.images.metadata.ImageMetadataStore
import com.simprints.infra.images.model.SecuredImageRef
import com.simprints.infra.images.usecase.GetUploaderUseCase
import com.simprints.infra.images.usecase.SamplePathConverter
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.FACE_CAPTURE
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.FINGER_CAPTURE
import com.simprints.infra.logging.Simber
import javax.inject.Inject

internal class ImageRepositoryImpl @Inject internal constructor(
    private val localDataSource: ImageLocalDataSource,
    private val metadataStore: ImageMetadataStore,
    private val samplePathConverter: SamplePathConverter,
    private val getSampleUploader: GetUploaderUseCase,
) : ImageRepository {
    override suspend fun storeSample(
        projectId: String,
        sessionId: String,
        modality: GeneralConfiguration.Modality,
        sampleId: String,
        fileExtension: String,
        sampleBytes: ByteArray,
        optionalMetadata: Map<String, String>,
    ): SecuredImageRef? {
        val logTag = if (modality == GeneralConfiguration.Modality.FACE) FACE_CAPTURE else FINGER_CAPTURE
        val relativePath = samplePathConverter.create(sessionId, modality, sampleId, fileExtension)

        Simber.d("Saving $modality sample to ${relativePath.compose()}", tag = logTag)

        return localDataSource
            .encryptAndStoreImage(sampleBytes, projectId, relativePath)
            .also {
                if (it == null) {
                    Simber.i("Saving image failed for captureId $sessionId", tag = logTag)
                } else {
                    // Only store metadata if the image was stored successfully
                    metadataStore.storeMetadata(relativePath, optionalMetadata)
                    // Also append mandatory metadata keys
                    metadataStore.storeMetadata(
                        relativePath,
                        mapOf(
                            META_KEY_FORMAT to fileExtension,
                        ),
                    )
                }
            }
    }

    override suspend fun getNumberOfImagesToUpload(projectId: String): Int = localDataSource.listImages(projectId).count()

    override suspend fun uploadStoredImagesAndDelete(projectId: String): Boolean = getSampleUploader().uploadAllSamples(projectId)

    override suspend fun deleteStoredImages() {
        metadataStore.deleteAllMetadata()
        for (image in localDataSource.listImages(null)) {
            localDataSource.deleteImage(image)
        }
    }

    companion object {
        private const val META_KEY_FORMAT = "format"
    }
}
