package com.simprints.moduleapi.fingerprint.responses

interface IFingerprintErrorResponse: IFingerprintResponse {
    val error: IFingerprintErrorReason
}

enum class IFingerprintErrorReason {
    UNEXPECTED_ERROR,
    BLUETOOTH_NOT_SUPPORTED,
    BLUETOOTH_NO_PERMISSION,
    FINGERPRINT_CONFIGURATION_ERROR
}
