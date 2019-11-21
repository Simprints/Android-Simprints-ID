package com.simprints.fingerprint.scanner.exceptions.unexpected

import com.simprints.fingerprint.exceptions.unexpected.FingerprintUnexpectedException

class BluetoothNotSupportedException(message: String = "BluetoothNotSupportedException") :
    FingerprintUnexpectedException(message)
