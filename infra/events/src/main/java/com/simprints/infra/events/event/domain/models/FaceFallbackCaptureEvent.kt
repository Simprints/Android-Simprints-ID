package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.events.event.domain.models.EventType.Companion.FACE_FALLBACK_CAPTURE_KEY
import com.simprints.infra.events.event.domain.models.EventType.FACE_FALLBACK_CAPTURE
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Keep
@Serializable
@SerialName(FACE_FALLBACK_CAPTURE_KEY)
data class FaceFallbackCaptureEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: FaceFallbackCapturePayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        startTime: Timestamp,
        endTime: Timestamp,
    ) : this(
        UUID.randomUUID().toString(),
        FaceFallbackCapturePayload(startTime, endTime, EVENT_VERSION),
        FACE_FALLBACK_CAPTURE,
    )

    @Keep
    @Serializable
    data class FaceFallbackCapturePayload(
        override val createdAt: Timestamp,
        override var endedAt: Timestamp?,
        override val eventVersion: Int,
        override val type: EventType = FACE_FALLBACK_CAPTURE,
    ) : EventPayload()

    companion object {
        const val EVENT_VERSION = 2
    }
}
