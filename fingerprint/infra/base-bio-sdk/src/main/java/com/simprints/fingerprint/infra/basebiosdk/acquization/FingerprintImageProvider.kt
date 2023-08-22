package com.simprints.fingerprint.infra.basebiosdk.acquization

import com.simprints.fingerprint.infra.basebiosdk.acquization.domain.AcquireFingerprintImageResponse

fun interface FingerprintImageProvider<ImageRequestSettings, ImageResponseMetadata> {
    fun acquireFingerprintImage(settings: ImageRequestSettings?): AcquireFingerprintImageResponse<ImageResponseMetadata>
}
