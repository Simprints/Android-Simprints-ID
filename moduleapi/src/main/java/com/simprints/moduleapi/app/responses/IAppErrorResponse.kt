package com.simprints.moduleapi.app.responses

interface IAppErrorResponse: IAppResponse {
    val reason: IAppErrorReason
}

enum class IAppErrorReason {
    DIFFERENT_PROJECT_ID_SIGNED_IN,
    DIFFERENT_USER_ID_SIGNED_IN,
    GUID_NOT_FOUND_ONLINE,
    BLUETOOTH_NOT_SUPPORTED,
    LOGIN_NOT_COMPLETE,
    UNEXPECTED_ERROR,
    ROOTED_DEVICE,
    ENROLMENT_LAST_BIOMETRICS_FAILED,
    FINGERPRINT_CONFIGURATION_ERROR
}
