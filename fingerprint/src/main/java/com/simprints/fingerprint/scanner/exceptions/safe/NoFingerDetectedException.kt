package com.simprints.fingerprint.scanner.exceptions.safe

import com.simprints.fingerprint.exceptions.safe.FingerprintSafeException

class NoFingerDetectedException(message: String = "NoFingerDetectedException") :
    FingerprintSafeException(message)
