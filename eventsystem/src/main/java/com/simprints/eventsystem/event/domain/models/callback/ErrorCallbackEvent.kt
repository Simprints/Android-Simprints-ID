package com.simprints.eventsystem.event.domain.models.callback

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventPayload
import com.simprints.eventsystem.event.domain.models.EventType
import com.simprints.eventsystem.event.domain.models.EventType.CALLBACK_ERROR
import com.simprints.moduleapi.app.responses.IAppErrorReason
import java.util.*

@Keep
data class ErrorCallbackEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: ErrorCallbackPayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        reason: ErrorCallbackPayload.Reason,
        labels: EventLabels = EventLabels()
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        ErrorCallbackPayload(createdAt, EVENT_VERSION, reason),
        CALLBACK_ERROR
    )

    @Keep
    data class ErrorCallbackPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val reason: Reason,
        override val type: EventType = CALLBACK_ERROR,
        override val endedAt: Long = 0
    ) : EventPayload() {

        @Keep
        enum class Reason {
            DIFFERENT_PROJECT_ID_SIGNED_IN,
            DIFFERENT_USER_ID_SIGNED_IN,
            GUID_NOT_FOUND_ONLINE,
            UNEXPECTED_ERROR,
            BLUETOOTH_NOT_SUPPORTED,
            LOGIN_NOT_COMPLETE,
            ENROLMENT_LAST_BIOMETRICS_FAILED,
            FACE_LICENSE_MISSING,
            FACE_LICENSE_INVALID,
            FINGERPRINT_CONFIGURATION_ERROR,
            BACKEND_MAINTENANCE_ERROR,
            FACE_CONFIGURATION_ERROR;

            companion object {
                fun fromAppResponseErrorReasonToEventReason(appResponseErrorReason: IAppErrorReason) =
                    when (appResponseErrorReason) {
                        IAppErrorReason.DIFFERENT_PROJECT_ID_SIGNED_IN -> DIFFERENT_PROJECT_ID_SIGNED_IN
                        IAppErrorReason.DIFFERENT_USER_ID_SIGNED_IN -> DIFFERENT_USER_ID_SIGNED_IN
                        IAppErrorReason.GUID_NOT_FOUND_ONLINE -> GUID_NOT_FOUND_ONLINE
                        IAppErrorReason.UNEXPECTED_ERROR -> UNEXPECTED_ERROR
                        IAppErrorReason.BLUETOOTH_NOT_SUPPORTED -> BLUETOOTH_NOT_SUPPORTED
                        IAppErrorReason.LOGIN_NOT_COMPLETE -> LOGIN_NOT_COMPLETE
                        IAppErrorReason.ENROLMENT_LAST_BIOMETRICS_FAILED -> ENROLMENT_LAST_BIOMETRICS_FAILED
                        IAppErrorReason.FACE_LICENSE_MISSING -> FACE_LICENSE_MISSING
                        IAppErrorReason.FACE_LICENSE_INVALID -> FACE_LICENSE_INVALID
                        IAppErrorReason.FINGERPRINT_CONFIGURATION_ERROR -> FINGERPRINT_CONFIGURATION_ERROR
                        IAppErrorReason.FACE_CONFIGURATION_ERROR -> FACE_CONFIGURATION_ERROR
                        IAppErrorReason.BACKEND_MAINTENANCE_ERROR -> BACKEND_MAINTENANCE_ERROR
                        IAppErrorReason.ROOTED_DEVICE -> throw Throwable("Can't convert from rooted device")
                    }
            }
        }
    }

    companion object {
        const val EVENT_VERSION = 1
    }
}
