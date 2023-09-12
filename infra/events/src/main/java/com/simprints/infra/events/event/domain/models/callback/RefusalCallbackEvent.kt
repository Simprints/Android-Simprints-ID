package com.simprints.infra.events.event.domain.models.callback

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.config.domain.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventLabels
import com.simprints.infra.events.event.domain.models.EventPayload
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.EventType.CALLBACK_REFUSAL
import java.util.UUID

@Keep
data class RefusalCallbackEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: RefusalCallbackPayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        reason: String,
        extra: String,
        labels: EventLabels = EventLabels()
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        RefusalCallbackPayload(createdAt, EVENT_VERSION, reason, extra),
        CALLBACK_REFUSAL
    )

    override fun getTokenizedFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    data class RefusalCallbackPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val reason: String,
        val extra: String,
        override val type: EventType = CALLBACK_REFUSAL,
        override val endedAt: Long = 0
    ) : EventPayload()

    companion object {
        const val EVENT_VERSION = 1
    }
}
