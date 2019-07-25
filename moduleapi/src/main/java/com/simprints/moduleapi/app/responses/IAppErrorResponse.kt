package com.simprints.moduleapi.app.responses

interface IAppErrorResponse: IAppResponse {
    val reason: IAppErrorReason
}

enum class IAppErrorReason {
    DIFFERENT_PROJECT_ID_SIGNED_IN,
    DIFFERENT_USER_ID_SIGNED_IN,
    GUID_NOT_FOUND_ONLINE,
    GUID_NOT_FOUND_OFFLINE,
    UNEXPECTED_ERROR,
    BLUETOOTH_NOT_SUPPORTED,
    SCANNER_LOW_BATTERY,
    UNKNOWN_BLUETOOTH_ISSUE
}
