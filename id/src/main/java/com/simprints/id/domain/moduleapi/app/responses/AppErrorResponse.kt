package com.simprints.id.domain.moduleapi.app.responses

import com.simprints.id.domain.alert.AlertType
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AppErrorResponse(val reason: Reason) : AppResponse {

    @IgnoredOnParcel override val type: AppResponseType = AppResponseType.ERROR

    enum class Reason {
        DIFFERENT_PROJECT_ID_SIGNED_IN,
        DIFFERENT_USER_ID_SIGNED_IN,
        UNEXPECTED_ERROR,
        BLUETOOTH_NOT_SUPPORTED,
        GUID_NOT_FOUND_ONLINE;

        companion object {
            fun fromDomainAlertTypeToAppErrorType(alertType: AlertType) =
                when (alertType) {
                    AlertType.DIFFERENT_PROJECT_ID_SIGNED_IN -> DIFFERENT_PROJECT_ID_SIGNED_IN
                    AlertType.DIFFERENT_USER_ID_SIGNED_IN -> DIFFERENT_USER_ID_SIGNED_IN
                    AlertType.UNEXPECTED_ERROR -> UNEXPECTED_ERROR
                }
        }
    }
}

