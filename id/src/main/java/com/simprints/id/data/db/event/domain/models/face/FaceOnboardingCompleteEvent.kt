package com.simprints.id.data.db.event.domain.models.face

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabels

import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.FACE_FALLBACK_CAPTURE
import com.simprints.id.data.db.event.domain.models.EventType.FACE_ONBOARDING_COMPLETE
import java.util.*

@Keep
data class FaceOnboardingCompleteEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: FaceOnboardingCompletePayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(
        startTime: Long,
        endTime: Long,
        labels: EventLabels = EventLabels()//StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        FaceOnboardingCompletePayload(startTime, endTime, EVENT_VERSION),
        FACE_ONBOARDING_COMPLETE)

    @Keep
    data class FaceOnboardingCompletePayload(
        override val createdAt: Long,
        override var endedAt: Long,
        override val eventVersion: Int
    ) : EventPayload(FACE_FALLBACK_CAPTURE, eventVersion, createdAt, endedAt)

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
