package com.simprints.id.data.db.event.domain.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.EventType
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
import java.util.*

@Keep
class ErrorCallbackEvent(
    createdAt: Long,
    reason: ErrorCallbackPayload.Reason,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(SessionIdLabel(sessionId)),
    ErrorCallbackPayload(createdAt, DEFAULT_EVENT_VERSION, reason)) {

    @Keep
    class ErrorCallbackPayload(createdAt: Long,
                               eventVersion: Int,
                               val reason: Reason) : EventPayload(EventType.CALLBACK_ERROR, eventVersion, createdAt) {

        enum class Reason {
            DIFFERENT_PROJECT_ID_SIGNED_IN,
            DIFFERENT_USER_ID_SIGNED_IN,
            GUID_NOT_FOUND_ONLINE,
            UNEXPECTED_ERROR,
            BLUETOOTH_NOT_SUPPORTED,
            LOGIN_NOT_COMPLETE,
            ENROLMENT_LAST_BIOMETRICS_FAILED;

            companion object {
                fun fromAppResponseErrorReasonToEventReason(appResponseErrorReason: AppErrorResponse.Reason) =
                    when (appResponseErrorReason) {
                        AppErrorResponse.Reason.DIFFERENT_PROJECT_ID_SIGNED_IN -> DIFFERENT_PROJECT_ID_SIGNED_IN
                        AppErrorResponse.Reason.DIFFERENT_USER_ID_SIGNED_IN -> DIFFERENT_USER_ID_SIGNED_IN
                        AppErrorResponse.Reason.GUID_NOT_FOUND_ONLINE -> GUID_NOT_FOUND_ONLINE
                        AppErrorResponse.Reason.UNEXPECTED_ERROR -> UNEXPECTED_ERROR
                        AppErrorResponse.Reason.BLUETOOTH_NOT_SUPPORTED -> BLUETOOTH_NOT_SUPPORTED
                        AppErrorResponse.Reason.LOGIN_NOT_COMPLETE -> LOGIN_NOT_COMPLETE
                        AppErrorResponse.Reason.ENROLMENT_LAST_BIOMETRICS_FAILED -> ENROLMENT_LAST_BIOMETRICS_FAILED
                    }
            }
        }
    }
}
