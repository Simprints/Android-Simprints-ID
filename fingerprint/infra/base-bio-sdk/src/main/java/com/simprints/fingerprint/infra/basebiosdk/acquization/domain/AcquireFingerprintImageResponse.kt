package com.simprints.fingerprint.infra.basebiosdk.acquization.domain

class AcquireFingerprintImageResponse<T>(
    val imageBytes: ByteArray,
    val imageMetadata: T?
)
