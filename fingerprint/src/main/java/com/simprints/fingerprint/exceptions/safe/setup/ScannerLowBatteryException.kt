package com.simprints.fingerprint.exceptions.safe.setup

import com.simprints.fingerprint.exceptions.safe.FingerprintSafeException

class ScannerLowBatteryException(message: String = "ScannerLowBatteryException") :
    FingerprintSafeException(message)
