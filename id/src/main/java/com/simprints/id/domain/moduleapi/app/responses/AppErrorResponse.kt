package com.simprints.id.domain.moduleapi.app.responses

import com.simprints.id.domain.alert.AlertType
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AppErrorResponse(val reason: AppErrorReason) : AppResponse {

    @IgnoredOnParcel override val type: AppResponseType = AppResponseType.ERROR
}

enum class AppErrorReason {
    DIFFERENT_PROJECT_ID_SIGNED_IN,
    DIFFERENT_USER_ID_SIGNED_IN,
    GUID_NOT_FOUND_ONLINE,
    GUID_NOT_FOUND_OFFLINE,
    UNEXPECTED_ERROR,
    BLUETOOTH_NOT_SUPPORTED,
    SCANNER_LOW_BATTERY,
    UNKNOWN_BLUETOOTH_ISSUE;

    companion object {
        fun fromDomainAlertTypeToAppErrorType(alertType: AlertType) =
            when (alertType) {
                AlertType.DIFFERENT_PROJECT_ID_SIGNED_IN -> DIFFERENT_PROJECT_ID_SIGNED_IN
                AlertType.DIFFERENT_USER_ID_SIGNED_IN -> DIFFERENT_USER_ID_SIGNED_IN
                AlertType.UNEXPECTED_ERROR -> UNEXPECTED_ERROR
            }
    }
}
