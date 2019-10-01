package com.simprints.fingerprint.scanner.domain

class CaptureFingerprintResponse(
    val template: ByteArray,
    val qualityScore: Int
)
