package com.simprints.id.domain.moduleapi.app.responses

import androidx.annotation.Keep
import com.simprints.id.alert.AlertType
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class AppErrorResponse(val reason: Reason) : AppResponse {

    @IgnoredOnParcel
    override val type: AppResponseType = AppResponseType.ERROR

    @Keep
    enum class Reason {
        DIFFERENT_PROJECT_ID_SIGNED_IN,
        DIFFERENT_USER_ID_SIGNED_IN,
        UNEXPECTED_ERROR,
        LOGIN_NOT_COMPLETE,
        BLUETOOTH_NOT_SUPPORTED,
        GUID_NOT_FOUND_ONLINE,
        ENROLMENT_LAST_BIOMETRICS_FAILED,
        FACE_LICENSE_MISSING,
        FACE_LICENSE_INVALID,
        FINGERPRINT_CONFIGURATION_ERROR,
        BLUETOOTH_NO_PERMISSION,
        BACKEND_MAINTENANCE_ERROR,
        PROJECT_ENDING,
        PROJECT_PAUSED,
        FACE_CONFIGURATION_ERROR;

        companion object {
            fun fromDomainAlertTypeToAppErrorType(alertType: AlertType) =
                when (alertType) {
                    AlertType.DIFFERENT_PROJECT_ID -> DIFFERENT_PROJECT_ID_SIGNED_IN
                    AlertType.DIFFERENT_USER_ID -> DIFFERENT_USER_ID_SIGNED_IN
                    AlertType.GOOGLE_PLAY_SERVICES_OUTDATED,
                    AlertType.MISSING_GOOGLE_PLAY_SERVICES,
                    AlertType.MISSING_OR_OUTDATED_GOOGLE_PLAY_STORE_APP,
                    AlertType.INTEGRITY_SERVICE_ERROR,
                    AlertType.UNEXPECTED_ERROR -> UNEXPECTED_ERROR
                    AlertType.PROJECT_ENDING -> PROJECT_ENDING
                    AlertType.PROJECT_PAUSED -> PROJECT_PAUSED
                }
        }
    }
}

