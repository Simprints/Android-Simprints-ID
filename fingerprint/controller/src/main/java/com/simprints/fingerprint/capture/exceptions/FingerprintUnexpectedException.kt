package com.simprints.fingerprint.capture.exceptions

internal class FingerprintUnexpectedException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
