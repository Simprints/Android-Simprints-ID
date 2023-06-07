package com.simprints.fingerprint.scanner.exceptions.safe

import com.simprints.fingerprint.exceptions.safe.FingerprintSafeException

class BluetoothNotSupportedException(message: String = "BluetoothNotSupportedException") :
    FingerprintSafeException(message)
