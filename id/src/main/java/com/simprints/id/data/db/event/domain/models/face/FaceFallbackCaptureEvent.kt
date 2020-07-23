package com.simprints.id.data.db.event.domain.models.face

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabels

import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.FACE_FALLBACK_CAPTURE
import java.util.*

@Keep
class FaceFallbackCaptureEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: FaceFallbackCapturePayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(
        startTime: Long,
        endTime: Long,
        labels: EventLabels = EventLabels() //StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        labels,
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
