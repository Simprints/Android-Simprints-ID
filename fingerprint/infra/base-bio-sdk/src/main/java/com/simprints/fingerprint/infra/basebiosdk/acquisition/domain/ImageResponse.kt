package com.simprints.fingerprint.infra.basebiosdk.acquisition.domain

import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireFingerprintImageResponse

@Suppress("unused") // ImageMetadata maybe used in other SDKs
class ImageResponse<T>(
    val imageBytes: ByteArray,
    val imageMetadata: T? = null,
)

fun ImageResponse<Unit>.toDomain() = AcquireFingerprintImageResponse(this.imageBytes)
