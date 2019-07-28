package com.simprints.fingerprint.exceptions.safe.scanner

import com.simprints.fingerprint.exceptions.safe.FingerprintSafeException

class MultipleScannersPairedException(message: String = "MultipleScannersPairedException") :
    FingerprintSafeException(message)
