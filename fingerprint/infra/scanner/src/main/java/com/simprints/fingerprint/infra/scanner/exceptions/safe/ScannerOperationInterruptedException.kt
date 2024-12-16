package com.simprints.fingerprint.infra.scanner.exceptions.safe

class ScannerOperationInterruptedException(
    message: String = "ScannerOperationInterruptedException",
) : ScannerSafeException(message)
