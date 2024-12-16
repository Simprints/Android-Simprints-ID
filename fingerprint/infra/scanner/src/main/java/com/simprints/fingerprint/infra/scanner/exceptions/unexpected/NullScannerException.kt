package com.simprints.fingerprint.infra.scanner.exceptions.unexpected

class NullScannerException(
    message: String = "NullScannerException",
) : UnexpectedScannerException(message)
