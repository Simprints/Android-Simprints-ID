package com.simprints.fingerprint.infra.basebiosdk.acquization

import com.simprints.fingerprint.infra.basebiosdk.acquization.domain.AcquireFingerprintImageResponse

fun interface FingerprintImageProvider<ImageRequestSettings, ImageResponseMetadata> {
    suspend fun acquireFingerprintImage(settings: ImageRequestSettings?): AcquireFingerprintImageResponse<ImageResponseMetadata>
}
