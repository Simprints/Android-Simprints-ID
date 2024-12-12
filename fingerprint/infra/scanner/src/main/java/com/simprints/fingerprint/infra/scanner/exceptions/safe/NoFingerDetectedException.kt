package com.simprints.fingerprint.infra.scanner.exceptions.safe

class NoFingerDetectedException(
    message: String,
) : ScannerSafeException("NoFingerDetectedException: $message")
