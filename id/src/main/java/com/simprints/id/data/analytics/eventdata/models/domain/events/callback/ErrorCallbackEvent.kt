package com.simprints.id.data.analytics.eventdata.models.domain.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.Event
import com.simprints.id.data.analytics.eventdata.models.domain.events.EventType
import com.simprints.id.data.analytics.eventdata.models.domain.events.callback.ErrorCallbackEvent.Reason.Companion.fromAppResponseErrorReasonToEventReason
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse

@Keep
class ErrorCallbackEvent(starTime: Long,
                         val reason: Reason): Event(EventType.CALLBACK_ERROR, starTime) {

    constructor(starTime: Long, reason: AppErrorResponse.Reason):
        this(starTime, fromAppResponseErrorReasonToEventReason(reason))

    enum class Reason {
        DIFFERENT_PROJECT_ID_SIGNED_IN,
        DIFFERENT_USER_ID_SIGNED_IN,
        GUID_NOT_FOUND_ONLINE,
        UNEXPECTED_ERROR,
        BLUETOOTH_NOT_SUPPORTED,
        LOGIN_NOT_COMPLETE,
        ROOTED_DEVICE;

        companion object {
            fun fromAppResponseErrorReasonToEventReason(appResponseErrorReason: AppErrorResponse.Reason) =
                when(appResponseErrorReason) {
                    AppErrorResponse.Reason.DIFFERENT_PROJECT_ID_SIGNED_IN -> DIFFERENT_PROJECT_ID_SIGNED_IN
                    AppErrorResponse.Reason.DIFFERENT_USER_ID_SIGNED_IN -> DIFFERENT_USER_ID_SIGNED_IN
                    AppErrorResponse.Reason.GUID_NOT_FOUND_ONLINE -> GUID_NOT_FOUND_ONLINE
                    AppErrorResponse.Reason.UNEXPECTED_ERROR -> UNEXPECTED_ERROR
                    AppErrorResponse.Reason.BLUETOOTH_NOT_SUPPORTED -> BLUETOOTH_NOT_SUPPORTED
                    AppErrorResponse.Reason.LOGIN_NOT_COMPLETE -> LOGIN_NOT_COMPLETE
                    AppErrorResponse.Reason.ROOTED_DEVICE -> ROOTED_DEVICE
                }
        }
    }
}

