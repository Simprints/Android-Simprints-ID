package com.simprints.fingerprint.scanner.exceptions.safe

import com.simprints.fingerprint.exceptions.FingerprintSimprintsException

class ScannerOperationInterruptedException(message: String = "ScannerOperationInterruptedException") :
    FingerprintSimprintsException(message)
