package com.simprints.fingerprint.scanner.exceptions.safe

import com.simprints.fingerprint.exceptions.safe.FingerprintSafeException

class MultiplePossibleScannersPairedException(message: String = "MultiplePossibleScannersPairedException") :
    FingerprintSafeException(message)
