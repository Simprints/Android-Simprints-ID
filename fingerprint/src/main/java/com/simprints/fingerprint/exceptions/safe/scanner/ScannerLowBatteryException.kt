package com.simprints.fingerprint.exceptions.safe.scanner

import com.simprints.fingerprint.exceptions.safe.FingerprintSafeException

class ScannerLowBatteryException(message: String = "ScannerLowBatteryException") :
    FingerprintSafeException(message)
