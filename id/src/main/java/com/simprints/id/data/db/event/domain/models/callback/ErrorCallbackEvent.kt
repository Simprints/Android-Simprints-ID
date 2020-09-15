package com.simprints.id.data.db.event.domain.models.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabels

import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.CALLBACK_ERROR
import com.simprints.id.data.db.event.local.models.DbEvent.Companion.DEFAULT_EVENT_VERSION
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
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
        labels: EventLabels = EventLabels() //StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        ErrorCallbackPayload(createdAt, EVENT_VERSION, reason),
        CALLBACK_ERROR)

    @Keep
    data class ErrorCallbackPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val reason: Reason,
        override val type: EventType = CALLBACK_ERROR,
        override val endedAt: Long = 0) : EventPayload() {

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
            FACE_LICENSE_INVALID,
            FINGERPRINT_CONFIGURATION_ERROR,
            FACE_CONFIGURATION_ERROR;

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
                        AppErrorResponse.Reason.FACE_LICENSE_MISSING -> FACE_LICENSE_MISSING
                        AppErrorResponse.Reason.FACE_LICENSE_INVALID -> FACE_LICENSE_INVALID
                        AppErrorResponse.Reason.SETUP_OFFLINE_DURING_MODALITY_DOWNLOAD -> SETUP_OFFLINE_DURING_MODALITY_DOWNLOAD
                        AppErrorResponse.Reason.SETUP_MODALITY_DOWNLOAD_CANCELLED -> SETUP_MODALITY_DOWNLOAD_CANCELLED
                        AppErrorResponse.Reason.FINGERPRINT_CONFIGURATION_ERROR -> FINGERPRINT_CONFIGURATION_ERROR
                        AppErrorResponse.Reason.FACE_CONFIGURATION_ERROR -> FACE_CONFIGURATION_ERROR
                    }
            }
        }
    }

    companion object {
        const val EVENT_VERSION = 1
    }
}
