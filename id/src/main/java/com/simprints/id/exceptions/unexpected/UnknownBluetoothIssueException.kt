package com.simprints.id.exceptions.unexpected

class UnknownBluetoothIssueException(message: String = "UnknownBluetoothIssueException") : // TODO : include ScannerError in exception
    UnexpectedException(message)
