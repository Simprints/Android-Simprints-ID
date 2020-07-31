package com.simprints.id.data.db.event.remote.models.callback

import com.simprints.id.data.db.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload
import com.simprints.id.data.db.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.*
import com.simprints.id.data.db.event.remote.models.callback.ApiErrorCallback.ApiReason
import io.realm.internal.Keep
import java.security.cert.CertPathValidatorException.Reason

@Keep
class ApiErrorCallback(val reason: ApiReason) : ApiCallback(ApiCallbackType.ERROR) {

    @Keep
    enum class ApiReason {
        DIFFERENT_PROJECT_ID_SIGNED_IN,
        DIFFERENT_USER_ID_SIGNED_IN,
        GUID_NOT_FOUND_ONLINE,
        @Deprecated("User can't leave the app anymore in case of GUID_NOT_FOUND_OFFLINE. He exits through the ExitForm.")
        GUID_NOT_FOUND_OFFLINE,
        UNEXPECTED_ERROR,
        BLUETOOTH_NOT_SUPPORTED,
        @Deprecated("User can't leave the app anymore in case of SCANNER_LOW_BATTERY. He exits through the ExitForm.")
        SCANNER_LOW_BATTERY,
        @Deprecated("Fingerprint module doesn't triggers it anymore")
        UNKNOWN_BLUETOOTH_ISSUE,
        LOGIN_NOT_COMPLETE,
        ENROLMENT_LAST_BIOMETRICS_FAILED,
        FACE_LICENSE_MISSING,
        FACE_LICENSE_INVALID
    }
}

fun ErrorCallbackPayload.Reason.fromDomainToApi() =
    when (this) {
        DIFFERENT_PROJECT_ID_SIGNED_IN -> ApiErrorCallback.ApiReason.DIFFERENT_PROJECT_ID_SIGNED_IN
        DIFFERENT_USER_ID_SIGNED_IN -> ApiErrorCallback.ApiReason.DIFFERENT_USER_ID_SIGNED_IN
        GUID_NOT_FOUND_ONLINE -> ApiErrorCallback.ApiReason.GUID_NOT_FOUND_ONLINE
        UNEXPECTED_ERROR -> ApiErrorCallback.ApiReason.UNEXPECTED_ERROR
        BLUETOOTH_NOT_SUPPORTED -> ApiErrorCallback.ApiReason.BLUETOOTH_NOT_SUPPORTED
        LOGIN_NOT_COMPLETE -> ApiErrorCallback.ApiReason.LOGIN_NOT_COMPLETE
        ENROLMENT_LAST_BIOMETRICS_FAILED -> ApiErrorCallback.ApiReason.ENROLMENT_LAST_BIOMETRICS_FAILED
        FACE_LICENSE_MISSING -> ApiErrorCallback.ApiReason.FACE_LICENSE_MISSING
        FACE_LICENSE_INVALID -> ApiErrorCallback.ApiReason.FACE_LICENSE_INVALID
        SETUP_OFFLINE_DURING_MODALITY_DOWNLOAD -> ApiErrorCallback.ApiReason.UNEXPECTED_ERROR
        SETUP_MODALITY_DOWNLOAD_CANCELLED -> ApiErrorCallback.ApiReason.UNEXPECTED_ERROR
        FINGERPRINT_CONFIGURATION_ERROR -> ApiErrorCallback.ApiReason.UNEXPECTED_ERROR
        FACE_CONFIGURATION_ERROR -> ApiErrorCallback.ApiReason.UNEXPECTED_ERROR
    }


fun ApiReason.fromApiToDomain(): Reason =
    when (this) {
        DIFFERENT_PROJECT_ID_SIGNED_IN -> ApiErrorCallback.ApiReason.DIFFERENT_PROJECT_ID_SIGNED_IN
        DIFFERENT_USER_ID_SIGNED_IN -> ApiErrorCallback.ApiReason.DIFFERENT_USER_ID_SIGNED_IN
        GUID_NOT_FOUND_ONLINE -> ApiErrorCallback.ApiReason.GUID_NOT_FOUND_ONLINE
        UNEXPECTED_ERROR -> ApiErrorCallback.ApiReason.UNEXPECTED_ERROR
        BLUETOOTH_NOT_SUPPORTED -> ApiErrorCallback.ApiReason.BLUETOOTH_NOT_SUPPORTED
        LOGIN_NOT_COMPLETE -> ApiErrorCallback.ApiReason.LOGIN_NOT_COMPLETE
        ENROLMENT_LAST_BIOMETRICS_FAILED -> ApiErrorCallback.ApiReason.ENROLMENT_LAST_BIOMETRICS_FAILED
        FACE_LICENSE_MISSING -> ApiErrorCallback.ApiReason.FACE_LICENSE_MISSING
        FACE_LICENSE_INVALID -> ApiErrorCallback.ApiReason.FACE_LICENSE_INVALID
        SETUP_OFFLINE_DURING_MODALITY_DOWNLOAD -> ApiErrorCallback.ApiReason.UNEXPECTED_ERROR
        SETUP_MODALITY_DOWNLOAD_CANCELLED -> ApiErrorCallback.ApiReason.UNEXPECTED_ERROR
        FINGERPRINT_CONFIGURATION_ERROR -> ApiErrorCallback.ApiReason.UNEXPECTED_ERROR
        FACE_CONFIGURATION_ERROR -> ApiErrorCallback.ApiReason.UNEXPECTED_ERROR
    }
