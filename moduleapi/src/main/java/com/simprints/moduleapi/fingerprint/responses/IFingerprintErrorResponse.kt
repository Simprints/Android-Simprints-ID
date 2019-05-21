package com.simprints.moduleapi.fingerprint.responses

import com.simprints.moduleapi.app.responses.IAppErrorType

interface IFingerprintErrorResponse: IFingerprintResponse {
    val error: IFingerprintErrorType
}

enum class IFingerprintErrorType {
    UNEXPECTED_ERROR,
    BLUETOOTH_NOT_SUPPORTED,
    SCANNER_LOW_BATTERY,
    UNKNOWN_BLUETOOTH_ISSUE
}
