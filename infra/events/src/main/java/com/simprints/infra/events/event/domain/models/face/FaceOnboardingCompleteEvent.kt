package com.simprints.infra.events.event.domain.models.face

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventPayload
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.EventType.FACE_ONBOARDING_COMPLETE
import java.util.UUID

@Keep
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

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
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
