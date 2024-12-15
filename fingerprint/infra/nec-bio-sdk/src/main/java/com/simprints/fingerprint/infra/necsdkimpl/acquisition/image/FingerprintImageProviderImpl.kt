package com.simprints.fingerprint.infra.necsdkimpl.acquisition.image

import com.simprints.fingerprint.infra.basebiosdk.acquisition.FingerprintImageProvider
import com.simprints.fingerprint.infra.basebiosdk.acquisition.domain.ImageResponse
import com.simprints.fingerprint.infra.basebiosdk.exceptions.BioSdkException
import javax.inject.Inject

internal class FingerprintImageProviderImpl @Inject constructor(
    private val imageCache: ProcessedImageCache,
) : FingerprintImageProvider<Unit, Unit> {
    /**
     * Acquire fingerprint image from the image cache
     * The image should be already captured by the time this method is called
     * return the image if it is not null or throw an exception
     *
     */
    override suspend fun acquireFingerprintImage(settings: Unit?): ImageResponse<Unit> =
        imageCache.recentlyCapturedImage?.let { ImageResponse(it) }
            ?: throw BioSdkException.CannotAcquireFingerprintImageException("Last captured image is null")
}
