package com.simprints.infra.events.event.domain.models.face

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventPayload
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.EventType.FACE_CAPTURE_CONFIRMATION
import com.simprints.infra.events.event.domain.models.face.FaceCaptureConfirmationEvent.FaceCaptureConfirmationPayload.Result
import java.util.UUID

@Keep
data class FaceCaptureConfirmationEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: FaceCaptureConfirmationPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        startTime: Timestamp,
        endTime: Timestamp,
        result: Result,
    ) : this(
        UUID.randomUUID().toString(),
        FaceCaptureConfirmationPayload(startTime, endTime, EVENT_VERSION, result),
        FACE_CAPTURE_CONFIRMATION,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    data class FaceCaptureConfirmationPayload(
        override val createdAt: Timestamp,
        override var endedAt: Timestamp?,
        override val eventVersion: Int,
        val result: Result,
        override val type: EventType = FACE_CAPTURE_CONFIRMATION,
    ) : EventPayload() {
        override fun toSafeString(): String = "result: $result"

        enum class Result {
            CONTINUE,
            RECAPTURE,
        }
    }

    companion object {
        const val EVENT_VERSION = 2
    }
}
