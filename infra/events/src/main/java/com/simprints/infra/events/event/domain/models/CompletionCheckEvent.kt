package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.infra.config.domain.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.COMPLETION_CHECK
import java.util.UUID

@Keep
data class CompletionCheckEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: CompletionCheckPayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        completed: Boolean,
        labels: EventLabels = EventLabels()
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        CompletionCheckPayload(createdAt, EVENT_VERSION, completed),
        COMPLETION_CHECK
    )

    override fun getTokenizedFields(): Map<TokenKeyType, String> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, String>) = this // No tokenized fields

    @Keep
    data class CompletionCheckPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val completed: Boolean,
        override val type: EventType = COMPLETION_CHECK,
        override val endedAt: Long = 0
    ) : EventPayload()

    companion object {
        const val EVENT_VERSION = 1
    }
}
