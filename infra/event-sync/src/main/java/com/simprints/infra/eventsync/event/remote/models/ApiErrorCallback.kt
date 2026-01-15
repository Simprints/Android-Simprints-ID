package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.ErrorCallbackEvent.ErrorCallbackPayload.Reason
import com.simprints.infra.events.event.domain.models.ErrorCallbackEvent.ErrorCallbackPayload.Reason.AGE_GROUP_NOT_SUPPORTED
import com.simprints.infra.events.event.domain.models.ErrorCallbackEvent.ErrorCallbackPayload.Reason.BACKEND_MAINTENANCE_ERROR
import com.simprints.infra.events.event.domain.models.ErrorCallbackEvent.ErrorCallbackPayload.Reason.BLUETOOTH_NOT_SUPPORTED
import com.simprints.infra.events.event.domain.models.ErrorCallbackEvent.ErrorCallbackPayload.Reason.BLUETOOTH_NO_PERMISSION
import com.simprints.infra.events.event.domain.models.ErrorCallbackEvent.ErrorCallbackPayload.Reason.DIFFERENT_PROJECT_ID_SIGNED_IN
import com.simprints.infra.events.event.domain.models.ErrorCallbackEvent.ErrorCallbackPayload.Reason.DIFFERENT_USER_ID_SIGNED_IN
import com.simprints.infra.events.event.domain.models.ErrorCallbackEvent.ErrorCallbackPayload.Reason.ENROLMENT_LAST_BIOMETRICS_FAILED
import com.simprints.infra.events.event.domain.models.ErrorCallbackEvent.ErrorCallbackPayload.Reason.FACE_CONFIGURATION_ERROR
import com.simprints.infra.events.event.domain.models.ErrorCallbackEvent.ErrorCallbackPayload.Reason.FINGERPRINT_CONFIGURATION_ERROR
import com.simprints.infra.events.event.domain.models.ErrorCallbackEvent.ErrorCallbackPayload.Reason.GUID_NOT_FOUND_OFFLINE
import com.simprints.infra.events.event.domain.models.ErrorCallbackEvent.ErrorCallbackPayload.Reason.GUID_NOT_FOUND_ONLINE
import com.simprints.infra.events.event.domain.models.ErrorCallbackEvent.ErrorCallbackPayload.Reason.LICENSE_INVALID
import com.simprints.infra.events.event.domain.models.ErrorCallbackEvent.ErrorCallbackPayload.Reason.LICENSE_MISSING
import com.simprints.infra.events.event.domain.models.ErrorCallbackEvent.ErrorCallbackPayload.Reason.LOGIN_NOT_COMPLETE
import com.simprints.infra.events.event.domain.models.ErrorCallbackEvent.ErrorCallbackPayload.Reason.PROJECT_ENDING
import com.simprints.infra.events.event.domain.models.ErrorCallbackEvent.ErrorCallbackPayload.Reason.PROJECT_PAUSED
import com.simprints.infra.events.event.domain.models.ErrorCallbackEvent.ErrorCallbackPayload.Reason.UNEXPECTED_ERROR
import com.simprints.infra.events.event.domain.models.EventType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@SerialName(EventType.CALLBACK_ERROR_KEY)
internal data class ApiErrorCallback(
    val reason: ApiReason,
    override val type: ApiCallbackType = ApiCallbackType.Error,
) : ApiCallback() {
    @Keep
    @Serializable
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

internal fun Reason.fromDomainToApi() = when (this) {
    DIFFERENT_PROJECT_ID_SIGNED_IN -> ApiErrorCallback.ApiReason.DIFFERENT_PROJECT_ID_SIGNED_IN
    DIFFERENT_USER_ID_SIGNED_IN -> ApiErrorCallback.ApiReason.DIFFERENT_USER_ID_SIGNED_IN
    GUID_NOT_FOUND_ONLINE -> ApiErrorCallback.ApiReason.GUID_NOT_FOUND_ONLINE
    GUID_NOT_FOUND_OFFLINE -> ApiErrorCallback.ApiReason.GUID_NOT_FOUND_OFFLINE
    UNEXPECTED_ERROR -> ApiErrorCallback.ApiReason.UNEXPECTED_ERROR
    BLUETOOTH_NOT_SUPPORTED -> ApiErrorCallback.ApiReason.BLUETOOTH_NOT_SUPPORTED
    LOGIN_NOT_COMPLETE -> ApiErrorCallback.ApiReason.LOGIN_NOT_COMPLETE
    ENROLMENT_LAST_BIOMETRICS_FAILED -> ApiErrorCallback.ApiReason.ENROLMENT_LAST_BIOMETRICS_FAILED
    LICENSE_MISSING -> ApiErrorCallback.ApiReason.LICENSE_MISSING
    LICENSE_INVALID -> ApiErrorCallback.ApiReason.LICENSE_INVALID
    FINGERPRINT_CONFIGURATION_ERROR -> ApiErrorCallback.ApiReason.UNEXPECTED_ERROR
    FACE_CONFIGURATION_ERROR -> ApiErrorCallback.ApiReason.UNEXPECTED_ERROR
    BACKEND_MAINTENANCE_ERROR -> ApiErrorCallback.ApiReason.BACKEND_MAINTENANCE_ERROR
    PROJECT_ENDING -> ApiErrorCallback.ApiReason.PROJECT_ENDING
    PROJECT_PAUSED -> ApiErrorCallback.ApiReason.PROJECT_PAUSED
    BLUETOOTH_NO_PERMISSION -> ApiErrorCallback.ApiReason.BLUETOOTH_NO_PERMISSION
    AGE_GROUP_NOT_SUPPORTED -> ApiErrorCallback.ApiReason.AGE_GROUP_NOT_SUPPORTED
}

internal fun ApiErrorCallback.ApiReason.fromApiToDomain(): Reason = when (this) {
    ApiErrorCallback.ApiReason.DIFFERENT_PROJECT_ID_SIGNED_IN -> DIFFERENT_PROJECT_ID_SIGNED_IN
    ApiErrorCallback.ApiReason.DIFFERENT_USER_ID_SIGNED_IN -> DIFFERENT_USER_ID_SIGNED_IN
    ApiErrorCallback.ApiReason.GUID_NOT_FOUND_ONLINE -> GUID_NOT_FOUND_ONLINE
    ApiErrorCallback.ApiReason.GUID_NOT_FOUND_OFFLINE -> GUID_NOT_FOUND_OFFLINE
    ApiErrorCallback.ApiReason.UNEXPECTED_ERROR -> UNEXPECTED_ERROR
    ApiErrorCallback.ApiReason.BLUETOOTH_NOT_SUPPORTED -> BLUETOOTH_NOT_SUPPORTED
    ApiErrorCallback.ApiReason.SCANNER_LOW_BATTERY -> UNEXPECTED_ERROR
    ApiErrorCallback.ApiReason.LOGIN_NOT_COMPLETE -> LOGIN_NOT_COMPLETE
    ApiErrorCallback.ApiReason.ENROLMENT_LAST_BIOMETRICS_FAILED -> ENROLMENT_LAST_BIOMETRICS_FAILED
    ApiErrorCallback.ApiReason.LICENSE_MISSING -> LICENSE_MISSING
    ApiErrorCallback.ApiReason.LICENSE_INVALID -> LICENSE_INVALID
    ApiErrorCallback.ApiReason.BACKEND_MAINTENANCE_ERROR -> BACKEND_MAINTENANCE_ERROR
    ApiErrorCallback.ApiReason.PROJECT_ENDING -> PROJECT_ENDING
    ApiErrorCallback.ApiReason.PROJECT_PAUSED -> PROJECT_PAUSED
    ApiErrorCallback.ApiReason.BLUETOOTH_NO_PERMISSION -> BLUETOOTH_NO_PERMISSION
    ApiErrorCallback.ApiReason.AGE_GROUP_NOT_SUPPORTED -> AGE_GROUP_NOT_SUPPORTED
}
