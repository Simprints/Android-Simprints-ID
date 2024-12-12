package com.simprints.fingerprint.infra.scanner.exceptions.safe

class ScannerLowBatteryException(
    message: String = "ScannerLowBatteryException",
) : ScannerSafeException(message)
