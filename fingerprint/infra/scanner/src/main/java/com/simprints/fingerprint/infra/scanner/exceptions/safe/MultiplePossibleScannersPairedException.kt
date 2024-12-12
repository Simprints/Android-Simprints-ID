package com.simprints.fingerprint.infra.scanner.exceptions.safe

class MultiplePossibleScannersPairedException(
    message: String = "MultiplePossibleScannersPairedException",
) : ScannerSafeException(message)
