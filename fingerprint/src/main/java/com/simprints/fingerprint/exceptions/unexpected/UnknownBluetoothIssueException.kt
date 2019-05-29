package com.simprints.fingerprint.exceptions.unexpected

class UnknownBluetoothIssueException(message: String = "UnknownBluetoothIssueException") : // TODO : include ScannerError in exception
    FingerprintUnexpectedException(message)
