package com.simprints.fingerprint.infra.scanner.exceptions

open class ScannerException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
