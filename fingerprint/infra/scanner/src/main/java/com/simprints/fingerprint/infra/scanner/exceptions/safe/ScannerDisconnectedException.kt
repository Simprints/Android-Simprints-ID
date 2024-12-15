package com.simprints.fingerprint.infra.scanner.exceptions.safe

class ScannerDisconnectedException(
    message: String = "ScannerDisconnectedException",
) : ScannerSafeException(message)
