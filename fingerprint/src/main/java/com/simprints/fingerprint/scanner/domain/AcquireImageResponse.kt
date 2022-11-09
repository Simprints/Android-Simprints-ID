package com.simprints.fingerprint.scanner.domain

/**
 * This class represents the response from acquiring the bytes of the fingerprint image that was
 * captured.
 *
 * @property imageBytes  the bytes of the captured fingerprint image
 */
class AcquireImageResponse(
    val imageBytes: ByteArray
)
