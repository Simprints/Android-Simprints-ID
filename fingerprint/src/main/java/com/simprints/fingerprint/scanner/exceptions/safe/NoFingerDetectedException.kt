package com.simprints.fingerprint.scanner.exceptions.safe

import com.simprints.fingerprint.exceptions.FingerprintSimprintsException

class NoFingerDetectedException(message: String = "NoFingerDetectedException") :
    FingerprintSimprintsException(message)
