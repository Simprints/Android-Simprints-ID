package com.simprints.id.data.db.event.domain.models.face

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabel
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.FACE_CAPTURE_RETRY
import java.util.*

@Keep
class FaceCaptureRetryEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val labels: MutableList<EventLabel>,
    override val payload: FaceCaptureRetryPayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(
        startTime: Long,
        endTime: Long,
        sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        mutableListOf<EventLabel>(SessionIdLabel(sessionId)),
        FaceCaptureRetryPayload(startTime, endTime, EVENT_VERSION),
        FACE_CAPTURE_RETRY)

    @Keep
    class FaceCaptureRetryPayload(
        override val createdAt: Long,
        override val endedAt: Long,
        override val eventVersion: Int
    ) : EventPayload(FACE_CAPTURE_RETRY, eventVersion, createdAt, endedAt)

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
