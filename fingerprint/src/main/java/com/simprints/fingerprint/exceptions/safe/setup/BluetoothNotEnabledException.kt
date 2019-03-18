package com.simprints.fingerprint.exceptions.safe.setup

import com.simprints.fingerprint.exceptions.safe.FingerprintSafeException

class BluetoothNotEnabledException(message: String = "BluetoothNotEnabledException") :
    FingerprintSafeException(message)
