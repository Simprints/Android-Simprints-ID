package com.simprints.eventsystem.event.domain.models.face

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventPayload
import com.simprints.eventsystem.event.domain.models.EventType
import com.simprints.eventsystem.event.domain.models.EventType.FACE_ONBOARDING_COMPLETE
import java.util.UUID

@Keep
data class FaceOnboardingCompleteEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: FaceOnboardingCompletePayload,
    override val type: EventType
) : Event() {

    constructor(
        startTime: Long,
        endTime: Long,
        labels: EventLabels = EventLabels()
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        FaceOnboardingCompletePayload(startTime, endTime, EVENT_VERSION),
        FACE_ONBOARDING_COMPLETE)

    @Keep
    data class FaceOnboardingCompletePayload(
        override val createdAt: Long,
        override var endedAt: Long,
        override val eventVersion: Int,
        override val type: EventType = FACE_ONBOARDING_COMPLETE
    ) : EventPayload()

    companion object {
        const val EVENT_VERSION = 1
    }
}
