package com.simprints.fingerprint.exceptions.safe.scanner

import com.simprints.fingerprint.exceptions.safe.FingerprintSafeException

class BluetoothNotEnabledException(message: String = "BluetoothNotEnabledException") :
    FingerprintSafeException(message)
