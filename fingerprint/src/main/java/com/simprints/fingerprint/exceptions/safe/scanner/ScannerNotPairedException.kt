package com.simprints.fingerprint.exceptions.safe.scanner

import com.simprints.fingerprint.exceptions.safe.FingerprintSafeException

class ScannerNotPairedException(message: String = "ScannerNotPairedException") :
    FingerprintSafeException(message)
