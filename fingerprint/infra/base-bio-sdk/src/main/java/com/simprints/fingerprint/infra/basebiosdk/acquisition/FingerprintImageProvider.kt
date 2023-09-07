package com.simprints.fingerprint.infra.basebiosdk.acquisition

import com.simprints.fingerprint.infra.basebiosdk.acquisition.domain.ImageResponse

fun interface FingerprintImageProvider<ImageRequestSettings, ImageResponseMetadata> {
    suspend fun acquireFingerprintImage(settings: ImageRequestSettings?): ImageResponse<ImageResponseMetadata>
}
