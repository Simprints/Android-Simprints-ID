package com.simprints.fingerprint.infra.basebiosdk.acquization.domain

class AcquireFingerprintTemplateResponse<T>(
    val template: ByteArray,
    val templateMetadata:T?
)
