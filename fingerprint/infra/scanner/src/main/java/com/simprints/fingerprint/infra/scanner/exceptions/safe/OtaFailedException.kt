package com.simprints.fingerprint.infra.scanner.exceptions.safe

class OtaFailedException(
    message: String = "OtaFailedException",
    cause: Throwable? = null,
) : ScannerSafeException(message, cause)
