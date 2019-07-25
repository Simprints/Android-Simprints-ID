package com.simprints.fingerprint.exceptions.safe.setup

import com.simprints.fingerprint.exceptions.safe.FingerprintSafeException

class ScannerNotPairedException(message: String = "ScannerNotPairedException") :
    FingerprintSafeException(message)
