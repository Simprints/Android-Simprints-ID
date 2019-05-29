package com.simprints.fingerprint.exceptions.safe.setup

import com.simprints.fingerprint.exceptions.safe.FingerprintSafeException

class MultipleScannersPairedException(message: String = "MultipleScannersPairedException") :
    FingerprintSafeException(message)
