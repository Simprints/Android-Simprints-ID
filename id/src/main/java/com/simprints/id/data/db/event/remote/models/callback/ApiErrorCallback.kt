package com.simprints.id.data.db.event.remote.models.callback

import com.simprints.id.data.db.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload
import com.simprints.id.data.db.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason
import com.simprints.id.data.db.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.*
import com.simprints.id.data.db.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.BLUETOOTH_NOT_SUPPORTED
import com.simprints.id.data.db.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.DIFFERENT_PROJECT_ID_SIGNED_IN
import com.simprints.id.data.db.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.DIFFERENT_USER_ID_SIGNED_IN
import com.simprints.id.data.db.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.ENROLMENT_LAST_BIOMETRICS_FAILED
import com.simprints.id.data.db.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.FACE_LICENSE_INVALID
import com.simprints.id.data.db.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.FACE_LICENSE_MISSING
import com.simprints.id.data.db.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.GUID_NOT_FOUND_ONLINE
import com.simprints.id.data.db.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.LOGIN_NOT_COMPLETE
import com.simprints.id.data.db.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.UNEXPECTED_ERROR
import com.simprints.id.data.db.event.remote.models.callback.ApiErrorCallback.ApiReason
import com.simprints.id.data.db.event.remote.models.callback.ApiErrorCallback.ApiReason.*
import io.realm.internal.Keep

@Keep
class ApiErrorCallback(val reason: ApiReason) : ApiCallback(ApiCallbackType.Error) {

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
        DIFFERENT_PROJECT_ID_SIGNED_IN -> ApiReason.DIFFERENT_PROJECT_ID_SIGNED_IN
        DIFFERENT_USER_ID_SIGNED_IN -> ApiReason.DIFFERENT_USER_ID_SIGNED_IN
        GUID_NOT_FOUND_ONLINE -> ApiReason.GUID_NOT_FOUND_ONLINE
        UNEXPECTED_ERROR -> ApiReason.UNEXPECTED_ERROR
        BLUETOOTH_NOT_SUPPORTED -> ApiReason.BLUETOOTH_NOT_SUPPORTED
        LOGIN_NOT_COMPLETE -> ApiReason.LOGIN_NOT_COMPLETE
        ENROLMENT_LAST_BIOMETRICS_FAILED -> ApiReason.ENROLMENT_LAST_BIOMETRICS_FAILED
        FACE_LICENSE_MISSING -> ApiReason.FACE_LICENSE_MISSING
        FACE_LICENSE_INVALID -> ApiReason.FACE_LICENSE_INVALID
        SETUP_OFFLINE_DURING_MODALITY_DOWNLOAD -> ApiReason.UNEXPECTED_ERROR
        SETUP_MODALITY_DOWNLOAD_CANCELLED -> ApiReason.UNEXPECTED_ERROR
        FINGERPRINT_CONFIGURATION_ERROR -> ApiReason.UNEXPECTED_ERROR
        FACE_CONFIGURATION_ERROR -> ApiReason.UNEXPECTED_ERROR
    }


fun ApiReason.fromApiToDomain(): Reason =
    when (this) {
        ApiReason.DIFFERENT_PROJECT_ID_SIGNED_IN -> DIFFERENT_PROJECT_ID_SIGNED_IN
        ApiReason.DIFFERENT_USER_ID_SIGNED_IN -> DIFFERENT_USER_ID_SIGNED_IN
        ApiReason.GUID_NOT_FOUND_ONLINE -> GUID_NOT_FOUND_ONLINE
        GUID_NOT_FOUND_OFFLINE -> UNEXPECTED_ERROR
        ApiReason.UNEXPECTED_ERROR -> UNEXPECTED_ERROR
        ApiReason.BLUETOOTH_NOT_SUPPORTED -> BLUETOOTH_NOT_SUPPORTED
        SCANNER_LOW_BATTERY -> UNEXPECTED_ERROR
        UNKNOWN_BLUETOOTH_ISSUE -> UNEXPECTED_ERROR
        ApiReason.LOGIN_NOT_COMPLETE -> LOGIN_NOT_COMPLETE
        ApiReason.ENROLMENT_LAST_BIOMETRICS_FAILED -> ENROLMENT_LAST_BIOMETRICS_FAILED
        ApiReason.FACE_LICENSE_MISSING -> FACE_LICENSE_MISSING
        ApiReason.FACE_LICENSE_INVALID -> FACE_LICENSE_INVALID
    }
