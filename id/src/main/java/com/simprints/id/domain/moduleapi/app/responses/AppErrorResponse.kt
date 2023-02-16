package com.simprints.id.domain.moduleapi.app.responses

import androidx.annotation.Keep
import com.simprints.id.domain.alert.AlertType
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
        BACKEND_MAINTENANCE_ERROR,
        FACE_CONFIGURATION_ERROR;

        companion object {
            fun fromDomainAlertTypeToAppErrorType(alertType: AlertType) =
                when (alertType) {
                    AlertType.DIFFERENT_PROJECT_ID_SIGNED_IN -> DIFFERENT_PROJECT_ID_SIGNED_IN
                    AlertType.DIFFERENT_USER_ID_SIGNED_IN -> DIFFERENT_USER_ID_SIGNED_IN
                    AlertType.GOOGLE_PLAY_SERVICES_OUTDATED,
                    AlertType.MISSING_GOOGLE_PLAY_SERVICES,
                    AlertType.MissingOrOutdatedGooglePlayStoreApp,
                    AlertType.INTEGRITY_SERVICE_ERROR,
                    AlertType.UNEXPECTED_ERROR -> UNEXPECTED_ERROR
                    AlertType.GUID_NOT_FOUND_ONLINE -> GUID_NOT_FOUND_ONLINE
                    AlertType.GUID_NOT_FOUND_OFFLINE ->
                        throw Throwable("No ErrorType associated. GUID_NOT_FOUND_OFFLINE should return a ExitForm, not a Error Response.")
                    AlertType.ENROLMENT_LAST_BIOMETRICS_FAILED -> ENROLMENT_LAST_BIOMETRICS_FAILED
                }
        }
    }
}

