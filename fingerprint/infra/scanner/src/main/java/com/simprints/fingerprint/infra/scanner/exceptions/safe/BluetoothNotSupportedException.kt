package com.simprints.fingerprint.infra.scanner.exceptions.safe

class BluetoothNotSupportedException(
    message: String = "BluetoothNotSupportedException",
) : ScannerSafeException(message)
