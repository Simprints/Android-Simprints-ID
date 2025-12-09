package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.SUSPICIOUS_INTENT
import java.util.UUID

@Keep
data class SuspiciousIntentEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: SuspiciousIntentPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        startTime: Timestamp,
        unexpectedExtras: Map<String, Any?>,
    ) : this(
        UUID.randomUUID().toString(),
        SuspiciousIntentPayload(startTime, EVENT_VERSION, unexpectedExtras),
        SUSPICIOUS_INTENT,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    data class SuspiciousIntentPayload(
        override val startTime: Timestamp,
        override val eventVersion: Int,
        val unexpectedExtras: Map<String, Any?>,
        override val endTime: Timestamp? = null,
        override val type: EventType = SUSPICIOUS_INTENT,
    ) : EventPayload() {
        override fun toSafeString(): String = unexpectedExtras.entries.joinToString(", ") {
            "${it.key}: ${it.value}"
        }
    }

    companion object {
        const val EVENT_VERSION = 2
    }
}
