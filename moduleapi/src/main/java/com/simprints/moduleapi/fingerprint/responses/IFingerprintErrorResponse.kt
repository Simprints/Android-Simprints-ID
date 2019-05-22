package com.simprints.moduleapi.fingerprint.responses

interface IFingerprintErrorResponse: IFingerprintResponse {
    val error: IFingerprintErrorReason
}

enum class IFingerprintErrorReason {
    UNEXPECTED_ERROR,
    BLUETOOTH_NOT_SUPPORTED,
    SCANNER_LOW_BATTERY,
    UNKNOWN_BLUETOOTH_ISSUE,
    GUID_NOT_FOUND_ONLINE,
    GUID_NOT_FOUND_OFFLINE
}
