package com.simprints.fingerprint.infra.scanner.exceptions.safe

class BluetoothNotEnabledException(
    message: String = "BluetoothNotEnabledException",
) : ScannerSafeException(message)
