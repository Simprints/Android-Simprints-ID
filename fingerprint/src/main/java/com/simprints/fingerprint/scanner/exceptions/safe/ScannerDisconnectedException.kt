package com.simprints.fingerprint.scanner.exceptions.safe

import com.simprints.fingerprint.exceptions.FingerprintSimprintsException

class ScannerDisconnectedException(message: String = "ScannerDisconnectedException") :
    FingerprintSimprintsException(message)
