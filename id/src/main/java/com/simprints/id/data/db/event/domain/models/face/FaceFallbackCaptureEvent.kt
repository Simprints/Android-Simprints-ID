package com.simprints.id.data.db.event.domain.models.face

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabel
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.FACE_FALLBACK_CAPTURE
import java.util.*

@Keep
class FaceFallbackCaptureEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val labels: MutableList<EventLabel>,
    override val payload: FaceFallbackCapturePayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(
        startTime: Long,
        endTime: Long,
        sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        mutableListOf<EventLabel>(SessionIdLabel(sessionId)),
        FaceFallbackCapturePayload(startTime, endTime, EVENT_VERSION),
        FACE_FALLBACK_CAPTURE)

    @Keep
    class FaceFallbackCapturePayload(
        override val createdAt: Long,
        override val endedAt: Long,
        override val eventVersion: Int
    ) : EventPayload(FACE_FALLBACK_CAPTURE, eventVersion, createdAt, endedAt)

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
