package com.simprints.fingerprint.scanner.exceptions.safe

import com.simprints.fingerprint.exceptions.safe.FingerprintSafeException

class MultipleScannersPairedException(message: String = "MultipleScannersPairedException") :
    FingerprintSafeException(message)
