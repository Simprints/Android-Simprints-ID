package com.simprints.id.data.db.event.domain.models.face

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabels

import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.FACE_CAPTURE_RETRY
import com.simprints.id.data.db.event.local.models.DbEvent.Companion.DEFAULT_EVENT_VERSION
import java.util.*

@Keep
data class FaceCaptureRetryEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: FaceCaptureRetryPayload,
    override val type: EventType
) : Event() {

    constructor(
        startTime: Long,
        endTime: Long,
        labels: EventLabels = EventLabels()
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        FaceCaptureRetryPayload(startTime, endTime, EVENT_VERSION),
        FACE_CAPTURE_RETRY)

    @Keep
    data class FaceCaptureRetryPayload(
        override val createdAt: Long,
        override var endedAt: Long,
        override val eventVersion: Int,
        override val type: EventType = FACE_CAPTURE_RETRY
    ) : EventPayload()

    companion object {
        const val EVENT_VERSION = 1
    }
}
