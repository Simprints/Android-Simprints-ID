package com.simprints.infra.events.event.domain.models.callback

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventPayload
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.EventType.CALLBACK_VERIFICATION
import java.util.UUID

@Keep
data class VerificationCallbackEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: VerificationCallbackPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        createdAt: Timestamp,
        score: CallbackComparisonScore,
    ) : this(
        UUID.randomUUID().toString(),
        VerificationCallbackPayload(createdAt, EVENT_VERSION, score),
        CALLBACK_VERIFICATION,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    data class VerificationCallbackPayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        val score: CallbackComparisonScore,
        override val endedAt: Timestamp? = null,
        override val type: EventType = CALLBACK_VERIFICATION,
    ) : EventPayload() {
        override fun toSafeString(): String = "confidence: ${score.confidence}"
    }

    companion object {
        const val EVENT_VERSION = 3
    }
}
