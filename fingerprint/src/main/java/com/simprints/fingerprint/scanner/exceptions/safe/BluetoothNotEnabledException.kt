package com.simprints.fingerprint.scanner.exceptions.safe

import com.simprints.fingerprint.exceptions.safe.FingerprintSafeException

class BluetoothNotEnabledException(message: String = "BluetoothNotEnabledException") :
    FingerprintSafeException(message)
