package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.INTENT_PARSING
import java.util.UUID

@Keep
data class IntentParsingEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: IntentParsingPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        createdAt: Timestamp,
        integration: IntentParsingPayload.IntegrationInfo,
    ) : this(
        UUID.randomUUID().toString(),
        IntentParsingPayload(createdAt, EVENT_VERSION, integration),
        INTENT_PARSING,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    data class IntentParsingPayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        val integration: IntegrationInfo,
        override val endedAt: Timestamp? = null,
        override val type: EventType = INTENT_PARSING,
    ) : EventPayload() {
        override fun toSafeString(): String = "integration: $integration"

        @Keep
        enum class IntegrationInfo {
            ODK,
            STANDARD,
            COMMCARE,
        }
    }

    companion object {
        const val EVENT_VERSION = 2
    }
}
