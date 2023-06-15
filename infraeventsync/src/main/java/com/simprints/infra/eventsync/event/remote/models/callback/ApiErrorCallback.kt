package com.simprints.infra.eventsync.event.remote.models.callback

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.*
import com.simprints.infra.eventsync.event.remote.models.callback.ApiErrorCallback.ApiReason
import com.simprints.infra.eventsync.event.remote.models.callback.ApiErrorCallback.ApiReason.GUID_NOT_FOUND_OFFLINE
import com.simprints.infra.eventsync.event.remote.models.callback.ApiErrorCallback.ApiReason.SCANNER_LOW_BATTERY

@Keep
internal data class ApiErrorCallback(val reason: ApiReason) : ApiCallback(ApiCallbackType.Error) {

    @Keep
    enum class ApiReason {
        DIFFERENT_PROJECT_ID_SIGNED_IN,
        DIFFERENT_USER_ID_SIGNED_IN,
        GUID_NOT_FOUND_ONLINE,

        @Deprecated("User can't leave the app anymore in case of GUID_NOT_FOUND_OFFLINE. He exits through the ExitForm.")
        GUID_NOT_FOUND_OFFLINE,
        UNEXPECTED_ERROR,
        BACKEND_MAINTENANCE_ERROR,
        BLUETOOTH_NOT_SUPPORTED,

        @Deprecated("User can't leave the app anymore in case of SCANNER_LOW_BATTERY. He exits through the ExitForm.")
        SCANNER_LOW_BATTERY,

        LOGIN_NOT_COMPLETE,
        ENROLMENT_LAST_BIOMETRICS_FAILED,
        FACE_LICENSE_MISSING,
        FACE_LICENSE_INVALID,
        PROJECT_PAUSED
    }
}

internal fun Reason.fromDomainToApi() =
    when (this) {
        DIFFERENT_PROJECT_ID_SIGNED_IN -> ApiReason.DIFFERENT_PROJECT_ID_SIGNED_IN
        DIFFERENT_USER_ID_SIGNED_IN -> ApiReason.DIFFERENT_USER_ID_SIGNED_IN
        GUID_NOT_FOUND_ONLINE -> ApiReason.GUID_NOT_FOUND_ONLINE
        UNEXPECTED_ERROR -> ApiReason.UNEXPECTED_ERROR
        BLUETOOTH_NOT_SUPPORTED -> ApiReason.BLUETOOTH_NOT_SUPPORTED
        LOGIN_NOT_COMPLETE -> ApiReason.LOGIN_NOT_COMPLETE
        ENROLMENT_LAST_BIOMETRICS_FAILED -> ApiReason.ENROLMENT_LAST_BIOMETRICS_FAILED
        FACE_LICENSE_MISSING -> ApiReason.FACE_LICENSE_MISSING
        FACE_LICENSE_INVALID -> ApiReason.FACE_LICENSE_INVALID
        FINGERPRINT_CONFIGURATION_ERROR -> ApiReason.UNEXPECTED_ERROR
        FACE_CONFIGURATION_ERROR -> ApiReason.UNEXPECTED_ERROR
        BACKEND_MAINTENANCE_ERROR -> ApiReason.BACKEND_MAINTENANCE_ERROR
        PROJECT_PAUSED -> ApiReason.PROJECT_PAUSED
    }


internal fun ApiReason.fromApiToDomain(): Reason =
    when (this) {
        ApiReason.DIFFERENT_PROJECT_ID_SIGNED_IN -> DIFFERENT_PROJECT_ID_SIGNED_IN
        ApiReason.DIFFERENT_USER_ID_SIGNED_IN -> DIFFERENT_USER_ID_SIGNED_IN
        ApiReason.GUID_NOT_FOUND_ONLINE -> GUID_NOT_FOUND_ONLINE
        GUID_NOT_FOUND_OFFLINE -> UNEXPECTED_ERROR
        ApiReason.UNEXPECTED_ERROR -> UNEXPECTED_ERROR
        ApiReason.BLUETOOTH_NOT_SUPPORTED -> BLUETOOTH_NOT_SUPPORTED
        SCANNER_LOW_BATTERY -> UNEXPECTED_ERROR
        ApiReason.LOGIN_NOT_COMPLETE -> LOGIN_NOT_COMPLETE
        ApiReason.ENROLMENT_LAST_BIOMETRICS_FAILED -> ENROLMENT_LAST_BIOMETRICS_FAILED
        ApiReason.FACE_LICENSE_MISSING -> FACE_LICENSE_MISSING
        ApiReason.FACE_LICENSE_INVALID -> FACE_LICENSE_INVALID
        ApiReason.BACKEND_MAINTENANCE_ERROR -> BACKEND_MAINTENANCE_ERROR
        ApiReason.PROJECT_PAUSED -> PROJECT_PAUSED
    }
