package com.simprints.fingerprint.exceptions.unexpected.scanner

import com.simprints.fingerprint.exceptions.unexpected.FingerprintUnexpectedException

class BluetoothNotSupportedException(message: String = "BluetoothNotSupportedException") :
    FingerprintUnexpectedException(message)
