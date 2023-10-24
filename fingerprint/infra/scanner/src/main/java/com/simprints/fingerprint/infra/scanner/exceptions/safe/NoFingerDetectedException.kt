package com.simprints.fingerprint.infra.scanner.exceptions.safe

class NoFingerDetectedException(
    message: String = "NoFingerDetectedException"
) : ScannerSafeException(message)
