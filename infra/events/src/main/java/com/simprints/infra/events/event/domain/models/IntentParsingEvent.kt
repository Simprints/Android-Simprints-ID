package com.simprints.infra.events.event.domain.models


import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.INTENT_PARSING
import java.util.UUID

@androidx.annotation.Keep
data class IntentParsingEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: IntentParsingPayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        integration: IntentParsingPayload.IntegrationInfo,
        labels: EventLabels = EventLabels()
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        IntentParsingPayload(createdAt, EVENT_VERSION, integration),
        INTENT_PARSING
    )

    override fun getTokenizedFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    data class IntentParsingPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val integration: IntegrationInfo,
        override val type: EventType = INTENT_PARSING,
        override val endedAt: Long = 0
    ) : EventPayload() {

        @Keep
        enum class IntegrationInfo {
            ODK,
            STANDARD,
            COMMCARE
        }
    }

    companion object {
        const val EVENT_VERSION = 1
    }
}
