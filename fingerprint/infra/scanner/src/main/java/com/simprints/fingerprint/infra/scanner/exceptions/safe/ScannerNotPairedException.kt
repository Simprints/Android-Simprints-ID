package com.simprints.fingerprint.infra.scanner.exceptions.safe

class ScannerNotPairedException(
    message: String = "ScannerNotPairedException",
) : ScannerSafeException(message)
