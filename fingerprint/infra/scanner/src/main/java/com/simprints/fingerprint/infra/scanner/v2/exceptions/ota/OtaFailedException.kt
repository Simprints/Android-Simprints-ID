package com.simprints.fingerprint.infra.scanner.v2.exceptions.ota

class OtaFailedException(
    message: String? = null,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
