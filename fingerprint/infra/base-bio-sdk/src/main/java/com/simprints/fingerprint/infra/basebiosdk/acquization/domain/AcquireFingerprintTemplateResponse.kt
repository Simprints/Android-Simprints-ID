package com.simprints.fingerprint.infra.basebiosdk.acquization.domain

class AcquireFingerprintTemplateResponse(
    val template: ByteArray,
    val format:String,
    val imageQualityScore: Int
)
