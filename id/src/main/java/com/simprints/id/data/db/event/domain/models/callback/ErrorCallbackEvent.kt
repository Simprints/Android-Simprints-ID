package com.simprints.id.data.db.event.domain.models.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabel
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.CALLBACK_ERROR
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
import java.util.*

@Keep
class ErrorCallbackEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val labels: MutableList<EventLabel>,
    override val payload: ErrorCallbackPayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(
        createdAt: Long,
        reason: ErrorCallbackPayload.Reason,
        sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        mutableListOf(SessionIdLabel(sessionId)),
        ErrorCallbackPayload(createdAt, EVENT_VERSION, reason),
        CALLBACK_ERROR)

    @Keep
    class ErrorCallbackPayload(createdAt: Long,
                               eventVersion: Int,
                               val reason: Reason) : EventPayload(CALLBACK_ERROR, eventVersion, createdAt) {

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

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
