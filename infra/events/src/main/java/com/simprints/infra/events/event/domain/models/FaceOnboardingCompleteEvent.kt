package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.events.event.domain.models.EventType.Companion.FACE_ONBOARDING_COMPLETE_KEY
import com.simprints.infra.events.event.domain.models.EventType.FACE_ONBOARDING_COMPLETE
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Keep
@Serializable
@SerialName(FACE_ONBOARDING_COMPLETE_KEY)
data class FaceOnboardingCompleteEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: FaceOnboardingCompletePayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        startTime: Timestamp,
        endTime: Timestamp,
    ) : this(
        UUID.randomUUID().toString(),
        FaceOnboardingCompletePayload(startTime, endTime, EVENT_VERSION),
        FACE_ONBOARDING_COMPLETE,
    )

    @Keep
    @Serializable
    data class FaceOnboardingCompletePayload(
        override val createdAt: Timestamp,
        override var endedAt: Timestamp?,
        override val eventVersion: Int,
        override val type: EventType = FACE_ONBOARDING_COMPLETE,
    ) : EventPayload()

    companion object {
        const val EVENT_VERSION = 2
    }
}
