package com.simprints.fingerprint.infra.basebiosdk.acquization.domain

class AcquireFingerprintImageResponse(
    val imageBytes: ByteArray,
    val imageMetadata: Map<String, Any>
)
