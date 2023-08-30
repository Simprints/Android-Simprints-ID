package com.simprints.fingerprint.infra.basebiosdk.acquization

import com.simprints.fingerprint.infra.basebiosdk.acquization.domain.ImageResponse

fun interface FingerprintImageProvider<ImageRequestSettings, ImageResponseMetadata> {
    suspend fun acquireFingerprintImage(settings: ImageRequestSettings?): ImageResponse<ImageResponseMetadata>
}
