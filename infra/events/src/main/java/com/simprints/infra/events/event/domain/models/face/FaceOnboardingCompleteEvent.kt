package com.simprints.infra.events.event.domain.models.face

import androidx.annotation.Keep
import com.simprints.infra.config.domain.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventLabels
import com.simprints.infra.events.event.domain.models.EventPayload
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.EventType.FACE_ONBOARDING_COMPLETE
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
        FACE_ONBOARDING_COMPLETE
    )

    override fun getTokenizedFields(): Map<TokenKeyType, String> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, String>) = this // No tokenized fields

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
