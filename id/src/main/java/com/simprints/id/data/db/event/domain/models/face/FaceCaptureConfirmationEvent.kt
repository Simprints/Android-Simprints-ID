package com.simprints.id.data.db.event.domain.models.face

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabel
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.FACE_CAPTURE_CONFIRMATION
import com.simprints.id.data.db.event.domain.models.face.FaceCaptureConfirmationEvent.FaceCaptureConfirmationPayload.Result
import java.util.*

@Keep
class FaceCaptureConfirmationEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val labels: MutableList<EventLabel>,
    override val payload: FaceCaptureConfirmationPayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(
        startTime: Long,
        endTime: Long,
        result: Result,
        sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        mutableListOf<EventLabel>(SessionIdLabel(sessionId)),
        FaceCaptureConfirmationPayload(startTime, endTime, EVENT_VERSION, result),
        FACE_CAPTURE_CONFIRMATION)


    @Keep
    class FaceCaptureConfirmationPayload(
        override val createdAt: Long,
        override val endedAt: Long,
        override val eventVersion: Int,
        val result: Result
    ) : EventPayload(FACE_CAPTURE_CONFIRMATION, eventVersion, createdAt, endedAt) {

        enum class Result {
            CONTINUE,
            RECAPTURE
        }
    }

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}

