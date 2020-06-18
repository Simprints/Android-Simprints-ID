package com.simprints.fingerprint.scanner.exceptions.safe

import com.simprints.fingerprint.exceptions.safe.FingerprintSafeException

class ScannerOperationInterruptedException(message: String = "ScannerOperationInterruptedException") :
    FingerprintSafeException(message)
