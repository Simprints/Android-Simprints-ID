package com.simprints.fingerprint.scanner.exceptions.safe

import com.simprints.fingerprint.exceptions.safe.FingerprintSafeException

class ScannerNotPairedException(message: String = "ScannerNotPairedException") :
    FingerprintSafeException(message)
