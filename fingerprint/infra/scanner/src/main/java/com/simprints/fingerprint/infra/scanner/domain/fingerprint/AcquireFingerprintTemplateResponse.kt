package com.simprints.fingerprint.infra.scanner.domain.fingerprint

/**
 * This class represents the response from a fingerprint capture.
 *
 * @property template  the generated biometric template that represents the fingerprint signature
 * @property imageQualityScore  the quality of the captured fingerprint image (in percentage).
 */
class AcquireFingerprintTemplateResponse(
    val template: ByteArray,
    val templateFormat: String,
    val imageQualityScore: Int,
)
