package com.simprints.fingerprint.infra.basebiosdk.acquisition.domain

@Suppress("unused") // ImageMetadata maybe used in other SDKs
class ImageResponse<T>(
    val imageBytes: ByteArray,
    val imageMetadata: T? = null
)
