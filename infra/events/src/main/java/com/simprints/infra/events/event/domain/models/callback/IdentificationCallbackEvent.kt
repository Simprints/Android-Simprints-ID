package com.simprints.infra.events.event.domain.models.callback

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventPayload
import com.simprints.infra.events.event.domain.models.EventType
import java.util.UUID

@Keep
data class IdentificationCallbackEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: IdentificationCallbackPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        createdAt: Timestamp,
        sessionId: String,
        scores: List<CallbackComparisonScore>,
    ) : this(
        UUID.randomUUID().toString(),
        IdentificationCallbackPayload(createdAt, EVENT_VERSION, sessionId, scores),
        EventType.CALLBACK_IDENTIFICATION,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    data class IdentificationCallbackPayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        val sessionId: String,
        val scores: List<CallbackComparisonScore>,
        override val endedAt: Timestamp? = null,
        override val type: EventType = EventType.CALLBACK_IDENTIFICATION,
    ) : EventPayload() {
        override fun toSafeString(): String = scores.joinToString(", ", prefix = "[", postfix = "]") {
            "${it.guid}: ${it.confidence}"
        }
    }

    companion object {
        const val EVENT_VERSION = 3
    }
}
