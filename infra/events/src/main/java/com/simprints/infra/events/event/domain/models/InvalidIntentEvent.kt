package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.INVALID_INTENT
import java.util.UUID

@Keep
data class InvalidIntentEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: InvalidIntentPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        creationTime: Timestamp,
        action: String,
        extras: Map<String, Any?>,
    ) : this(
        UUID.randomUUID().toString(),
        InvalidIntentPayload(creationTime, EVENT_VERSION, action, extras),
        INVALID_INTENT,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    data class InvalidIntentPayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        val action: String,
        val extras: Map<String, Any?>,
        override val endedAt: Timestamp? = null,
        override val type: EventType = INVALID_INTENT,
    ) : EventPayload() {
        override fun toSafeString(): String = "action: $action, extras: $extras"
    }

    companion object {
        const val EVENT_VERSION = 2
    }
}
