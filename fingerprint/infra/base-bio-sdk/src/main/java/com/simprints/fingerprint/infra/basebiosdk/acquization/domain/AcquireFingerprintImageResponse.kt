package com.simprints.fingerprint.infra.basebiosdk.acquization.domain

@Suppress("unused") // ImageMetadata maybe used in other SDKs
class AcquireFingerprintImageResponse<T>(
    val imageBytes: ByteArray,
    val imageMetadata: T? = null
)
