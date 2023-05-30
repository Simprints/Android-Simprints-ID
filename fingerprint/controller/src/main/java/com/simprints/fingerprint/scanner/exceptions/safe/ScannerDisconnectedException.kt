package com.simprints.fingerprint.scanner.exceptions.safe

import com.simprints.fingerprint.exceptions.safe.FingerprintSafeException

class ScannerDisconnectedException(message: String = "ScannerDisconnectedException") :
    FingerprintSafeException(message)
