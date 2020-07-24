package com.simprints.id.data.db.event.domain.models.face

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabels

import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.FACE_CAPTURE_RETRY
import java.util.*

@Keep
class FaceCaptureRetryEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: FaceCaptureRetryPayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(
        startTime: Long,
        endTime: Long,
        labels: EventLabels = EventLabels() //StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        FaceCaptureRetryPayload(startTime, endTime, EVENT_VERSION),
        FACE_CAPTURE_RETRY)

    @Keep
    class FaceCaptureRetryPayload(
        override val createdAt: Long,
        override var endedAt: Long,
        override val eventVersion: Int
    ) : EventPayload(FACE_CAPTURE_RETRY, eventVersion, createdAt, endedAt)

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
