package com.simprints.infra.eventsync.event.remote.models.callback

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.BACKEND_MAINTENANCE_ERROR
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.BLUETOOTH_NOT_SUPPORTED
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.BLUETOOTH_NO_PERMISSION
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.DIFFERENT_PROJECT_ID_SIGNED_IN
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.DIFFERENT_USER_ID_SIGNED_IN
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.ENROLMENT_LAST_BIOMETRICS_FAILED
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.FACE_CONFIGURATION_ERROR
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.FINGERPRINT_CONFIGURATION_ERROR
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.GUID_NOT_FOUND_OFFLINE
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.GUID_NOT_FOUND_ONLINE
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.LICENSE_INVALID
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.LICENSE_MISSING
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.LOGIN_NOT_COMPLETE
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.PROJECT_ENDING
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.PROJECT_PAUSED
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.AGE_GROUP_NOT_SUPPORTED
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.UNEXPECTED_ERROR
import com.simprints.infra.eventsync.event.remote.models.callback.ApiErrorCallback.ApiReason
import com.simprints.infra.eventsync.event.remote.models.callback.ApiErrorCallback.ApiReason.SCANNER_LOW_BATTERY

@Keep
internal data class ApiErrorCallback(val reason: ApiReason) : ApiCallback(ApiCallbackType.Error) {

    @Keep
    enum class ApiReason {
        DIFFERENT_PROJECT_ID_SIGNED_IN,
        DIFFERENT_USER_ID_SIGNED_IN,
        GUID_NOT_FOUND_ONLINE,

        GUID_NOT_FOUND_OFFLINE,
        UNEXPECTED_ERROR,
        BACKEND_MAINTENANCE_ERROR,
        BLUETOOTH_NOT_SUPPORTED,

        @Deprecated("User can't leave the app anymore in case of SCANNER_LOW_BATTERY. He exits through the ExitForm.")
        SCANNER_LOW_BATTERY,

        LOGIN_NOT_COMPLETE,
        ENROLMENT_LAST_BIOMETRICS_FAILED,
        LICENSE_MISSING,
        LICENSE_INVALID,
        PROJECT_ENDING,
        PROJECT_PAUSED,
        BLUETOOTH_NO_PERMISSION,
        AGE_GROUP_NOT_SUPPORTED,
    }
}

internal fun Reason.fromDomainToApi() =
    when (this) {
        DIFFERENT_PROJECT_ID_SIGNED_IN -> ApiReason.DIFFERENT_PROJECT_ID_SIGNED_IN
        DIFFERENT_USER_ID_SIGNED_IN -> ApiReason.DIFFERENT_USER_ID_SIGNED_IN
        GUID_NOT_FOUND_ONLINE -> ApiReason.GUID_NOT_FOUND_ONLINE
        GUID_NOT_FOUND_OFFLINE -> ApiReason.GUID_NOT_FOUND_OFFLINE
        UNEXPECTED_ERROR -> ApiReason.UNEXPECTED_ERROR
        BLUETOOTH_NOT_SUPPORTED -> ApiReason.BLUETOOTH_NOT_SUPPORTED
        LOGIN_NOT_COMPLETE -> ApiReason.LOGIN_NOT_COMPLETE
        ENROLMENT_LAST_BIOMETRICS_FAILED -> ApiReason.ENROLMENT_LAST_BIOMETRICS_FAILED
        LICENSE_MISSING -> ApiReason.LICENSE_MISSING
        LICENSE_INVALID -> ApiReason.LICENSE_INVALID
        FINGERPRINT_CONFIGURATION_ERROR -> ApiReason.UNEXPECTED_ERROR
        FACE_CONFIGURATION_ERROR -> ApiReason.UNEXPECTED_ERROR
        BACKEND_MAINTENANCE_ERROR -> ApiReason.BACKEND_MAINTENANCE_ERROR
        PROJECT_ENDING -> ApiReason.PROJECT_ENDING
        PROJECT_PAUSED -> ApiReason.PROJECT_PAUSED
        BLUETOOTH_NO_PERMISSION -> ApiReason.BLUETOOTH_NO_PERMISSION
        AGE_GROUP_NOT_SUPPORTED -> ApiReason.AGE_GROUP_NOT_SUPPORTED
    }


internal fun ApiReason.fromApiToDomain(): Reason =
    when (this) {
        ApiReason.DIFFERENT_PROJECT_ID_SIGNED_IN -> DIFFERENT_PROJECT_ID_SIGNED_IN
        ApiReason.DIFFERENT_USER_ID_SIGNED_IN -> DIFFERENT_USER_ID_SIGNED_IN
        ApiReason.GUID_NOT_FOUND_ONLINE -> GUID_NOT_FOUND_ONLINE
        ApiReason.GUID_NOT_FOUND_OFFLINE -> GUID_NOT_FOUND_OFFLINE
        ApiReason.UNEXPECTED_ERROR -> UNEXPECTED_ERROR
        ApiReason.BLUETOOTH_NOT_SUPPORTED -> BLUETOOTH_NOT_SUPPORTED
        SCANNER_LOW_BATTERY -> UNEXPECTED_ERROR
        ApiReason.LOGIN_NOT_COMPLETE -> LOGIN_NOT_COMPLETE
        ApiReason.ENROLMENT_LAST_BIOMETRICS_FAILED -> ENROLMENT_LAST_BIOMETRICS_FAILED
        ApiReason.LICENSE_MISSING -> LICENSE_MISSING
        ApiReason.LICENSE_INVALID -> LICENSE_INVALID
        ApiReason.BACKEND_MAINTENANCE_ERROR -> BACKEND_MAINTENANCE_ERROR
        ApiReason.PROJECT_ENDING -> PROJECT_ENDING
        ApiReason.PROJECT_PAUSED -> PROJECT_PAUSED
        ApiReason.BLUETOOTH_NO_PERMISSION -> BLUETOOTH_NO_PERMISSION
        ApiReason.AGE_GROUP_NOT_SUPPORTED -> AGE_GROUP_NOT_SUPPORTED
    }
