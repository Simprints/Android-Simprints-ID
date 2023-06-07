package com.simprints.fingerprint.scanner.domain

/**
 * This class represents the response from a fingerprint capture.
 *
 * @property template  the generated biometric template that represents the fingerprint signature
 * @property imageQualityScore  the quality of the captured fingerprint image (in percentage).
 */
class CaptureFingerprintResponse(
    val template: ByteArray,
    val imageQualityScore: Int
)
