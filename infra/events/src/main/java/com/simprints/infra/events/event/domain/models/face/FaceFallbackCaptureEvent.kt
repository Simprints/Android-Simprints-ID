package com.simprints.infra.events.event.domain.models.face

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizedString
import com.simprints.infra.config.domain.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventLabels
import com.simprints.infra.events.event.domain.models.EventPayload
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.EventType.FACE_FALLBACK_CAPTURE
import java.util.UUID

@Keep
data class FaceFallbackCaptureEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: FaceFallbackCapturePayload,
    override val type: EventType
) : Event() {

    constructor(
        startTime: Long,
        endTime: Long,
        labels: EventLabels = EventLabels()
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        FaceFallbackCapturePayload(startTime, endTime, EVENT_VERSION),
        FACE_FALLBACK_CAPTURE
    )

    override fun getTokenizedFields(): Map<TokenKeyType, TokenizedString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizedString>) = this // No tokenized fields

    @Keep
    data class FaceFallbackCapturePayload(
        override val createdAt: Long,
        override var endedAt: Long,
        override val eventVersion: Int,
        override val type: EventType = FACE_FALLBACK_CAPTURE
    ) : EventPayload()

    companion object {
        const val EVENT_VERSION = 1
    }
}
