package com.simprints.infra.events.event.domain.models.callback

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventPayload
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.EventType.CALLBACK_REFUSAL
import java.util.UUID

@Keep
data class RefusalCallbackEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: RefusalCallbackPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        createdAt: Timestamp,
        reason: String,
        extra: String,
    ) : this(
        UUID.randomUUID().toString(),
        RefusalCallbackPayload(createdAt, EVENT_VERSION, reason, extra),
        CALLBACK_REFUSAL,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    data class RefusalCallbackPayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        val reason: String,
        val extra: String,
        override val endedAt: Timestamp? = null,
        override val type: EventType = CALLBACK_REFUSAL,
    ) : EventPayload() {
        override fun toSafeString(): String = "reason: $reason, extra: $extra"
    }

    companion object {
        const val EVENT_VERSION = 3
    }
}
