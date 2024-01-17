package com.simprints.fingerprint.infra.necsdkimpl.acquisition.image

import com.simprints.fingerprint.infra.basebiosdk.acquisition.FingerprintImageProvider
import com.simprints.fingerprint.infra.basebiosdk.acquisition.domain.ImageResponse
import com.simprints.fingerprint.infra.basebiosdk.exceptions.BioSdkException
import javax.inject.Inject

class FingerprintImageProviderImpl @Inject constructor(
    private val imageCache: ImageCache
) : FingerprintImageProvider<Unit, Unit> {

    /**
     * Acquire fingerprint image from the image cache
     * return it if it is not null or throw an exception if it is null
     * (this is the case when we have not captured any image yet)
     */
    override suspend fun acquireFingerprintImage(settings: Unit?): ImageResponse<Unit> =
        imageCache.lastCaptureImage?.let { ImageResponse(it) }
            ?: throw BioSdkException.CannotAcquireFingerprintImageException("Last captured image is null")
}
