package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.COMPLETION_CHECK
import java.util.UUID

@Keep
data class CompletionCheckEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: CompletionCheckPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        createdAt: Timestamp,
        completed: Boolean,
    ) : this(
        UUID.randomUUID().toString(),
        CompletionCheckPayload(createdAt, EVENT_VERSION, completed),
        COMPLETION_CHECK,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    data class CompletionCheckPayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        val completed: Boolean,
        override val endedAt: Timestamp? = null,
        override val type: EventType = COMPLETION_CHECK,
    ) : EventPayload() {
        override fun toSafeString(): String = "completed: $completed"
    }

    companion object {
        const val EVENT_VERSION = 2
    }
}
