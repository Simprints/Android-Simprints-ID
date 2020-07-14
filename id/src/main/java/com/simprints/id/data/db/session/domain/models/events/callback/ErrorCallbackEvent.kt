package com.simprints.id.data.db.session.domain.models.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.Event
import com.simprints.id.data.db.session.domain.models.events.EventType
import com.simprints.id.data.db.session.domain.models.events.callback.ErrorCallbackEvent.Reason.Companion.fromAppResponseErrorReasonToEventReason
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse

@Keep
class ErrorCallbackEvent(startTime: Long,
                         val reason: Reason): Event(EventType.CALLBACK_ERROR, startTime) {

    constructor(startTime: Long, reason: AppErrorResponse.Reason):
        this(startTime, fromAppResponseErrorReasonToEventReason(reason))

    enum class Reason {
        DIFFERENT_PROJECT_ID_SIGNED_IN,
        DIFFERENT_USER_ID_SIGNED_IN,
        GUID_NOT_FOUND_ONLINE,
        SETUP_OFFLINE_DURING_MODALITY_DOWNLOAD,
        SETUP_MODALITY_DOWNLOAD_CANCELLED,
        UNEXPECTED_ERROR,
        BLUETOOTH_NOT_SUPPORTED,
        LOGIN_NOT_COMPLETE,
        ENROLMENT_LAST_BIOMETRICS_FAILED,
        FACE_LICENSE_MISSING,
        FACE_LICENSE_INVALID;

        companion object {
            fun fromAppResponseErrorReasonToEventReason(appResponseErrorReason: AppErrorResponse.Reason) =
                when(appResponseErrorReason) {
                    AppErrorResponse.Reason.DIFFERENT_PROJECT_ID_SIGNED_IN -> DIFFERENT_PROJECT_ID_SIGNED_IN
                    AppErrorResponse.Reason.DIFFERENT_USER_ID_SIGNED_IN -> DIFFERENT_USER_ID_SIGNED_IN
                    AppErrorResponse.Reason.GUID_NOT_FOUND_ONLINE -> GUID_NOT_FOUND_ONLINE
                    AppErrorResponse.Reason.UNEXPECTED_ERROR -> UNEXPECTED_ERROR
                    AppErrorResponse.Reason.BLUETOOTH_NOT_SUPPORTED -> BLUETOOTH_NOT_SUPPORTED
                    AppErrorResponse.Reason.LOGIN_NOT_COMPLETE -> LOGIN_NOT_COMPLETE
                    AppErrorResponse.Reason.ENROLMENT_LAST_BIOMETRICS_FAILED -> ENROLMENT_LAST_BIOMETRICS_FAILED
                    AppErrorResponse.Reason.FACE_LICENSE_MISSING -> FACE_LICENSE_MISSING
                    AppErrorResponse.Reason.FACE_LICENSE_INVALID -> FACE_LICENSE_INVALID
                    AppErrorResponse.Reason.SETUP_OFFLINE_DURING_MODALITY_DOWNLOAD -> SETUP_OFFLINE_DURING_MODALITY_DOWNLOAD
                    AppErrorResponse.Reason.SETUP_MODALITY_DOWNLOAD_CANCELLED -> SETUP_MODALITY_DOWNLOAD_CANCELLED
                }
        }
    }
}

