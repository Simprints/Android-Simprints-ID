package com.simprints.fingerprint.infra.scanner.domain.fingerprint

/**
 * This class represents the response from acquiring the bytes of the fingerprint image that was
 * captured.
 *
 * @property imageBytes  the bytes of the captured fingerprint image
 */
class AcquireFingerprintImageResponse(
    val imageBytes: ByteArray,
)
