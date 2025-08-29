package com.simprints.infra.images.remote.signedurl.usecase

import com.simprints.core.domain.image.SecuredImageRef
import com.simprints.infra.images.local.ImageLocalDataSource
import com.simprints.infra.images.metadata.ImageMetadataStore
import com.simprints.infra.images.remote.signedurl.SampleUploadData
import com.simprints.infra.images.usecase.CalculateFileMd5AndSizeUseCase
import com.simprints.infra.images.usecase.SamplePathConverter
import javax.inject.Inject

internal class PrepareImageUploadDataUseCase @Inject constructor(
    private val localDataSource: ImageLocalDataSource,
    private val calculateFileMd5AndSize: CalculateFileMd5AndSizeUseCase,
    private val samplePathUtil: SamplePathConverter,
    private val metadataStore: ImageMetadataStore,
) {
    suspend operator fun invoke(imageRef: SecuredImageRef): SampleUploadData? = localDataSource
        .decryptImage(imageRef)
        ?.use { stream -> calculateFileMd5AndSize(stream) }
        ?.let { (md5, size) ->
            val (sessionId, modality, sampleId) = samplePathUtil.extract(imageRef.relativePath)
                ?: return null
            val metadata = metadataStore.getMetadata(imageRef.relativePath)

            SampleUploadData(
                imageRef = imageRef,
                sampleId = sampleId,
                sessionId = sessionId,
                md5 = md5,
                size = size,
                modality = modality.name,
                metadata = metadata,
            )
        }
}
