package com.simprints.id.exceptions.unexpected

class UnknownBluetoothIssueException(message: String = "UnknownBluetoothIssueException") : // TODO : include SCANNER_ERROR in exception
    UnexpectedException(message)
