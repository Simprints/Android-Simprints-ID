package com.simprints.fingerprint.scanner.exceptions.safe

import com.simprints.fingerprint.exceptions.safe.FingerprintSafeException

class ScannerLowBatteryException(message: String = "ScannerLowBatteryException") :
    FingerprintSafeException(message)
