package com.simprints.id.domain.moduleapi.app.responses

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AppErrorResponse(val error: AppErrorType) : AppResponse {

    @IgnoredOnParcel override val type: AppResponseType = AppResponseType.ERROR
}

enum class AppErrorType {
    DIFFERENT_PROJECT_ID_SIGNED_IN,
    DIFFERENT_USER_ID_SIGNED_IN,
    GUID_NOT_FOUND_ONLINE,
    GUID_NOT_FOUND_OFFLINE,
    UNEXPECTED_ERROR,
    BLUETOOTH_NOT_SUPPORTED,
    SCANNER_LOW_BATTERY,
    UNKNOWN_BLUETOOTH_ISSUE
}
