package com.simprints.eventsystem.event.domain.models.face

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventPayload
import com.simprints.eventsystem.event.domain.models.EventType
import com.simprints.eventsystem.event.domain.models.EventType.FACE_CAPTURE_CONFIRMATION
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureConfirmationEvent.FaceCaptureConfirmationPayload.Result
import java.util.*

@Keep
data class FaceCaptureConfirmationEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: FaceCaptureConfirmationPayload,
    override val type: EventType
) : Event() {

    constructor(
        startTime: Long,
        endTime: Long,
        result: Result,
        labels: EventLabels = EventLabels()
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        FaceCaptureConfirmationPayload(startTime, endTime, EVENT_VERSION, result),
        FACE_CAPTURE_CONFIRMATION)


    @Keep
    data class FaceCaptureConfirmationPayload(
        override val createdAt: Long,
        override var endedAt: Long,
        override val eventVersion: Int,
        val result: Result,
        override val type: EventType = FACE_CAPTURE_CONFIRMATION
    ) : EventPayload() {

        enum class Result {
            CONTINUE,
            RECAPTURE
        }
    }

    companion object {
        const val EVENT_VERSION = 1
    }
}

