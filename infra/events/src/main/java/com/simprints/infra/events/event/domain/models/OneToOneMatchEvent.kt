package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.ONE_TO_ONE_MATCH
import java.util.UUID

@Keep
data class OneToOneMatchEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: OneToOneMatchPayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        endTime: Long,
        candidateId: String,
        matcher: String,
        result: MatchEntry?,
        fingerComparisonStrategy: FingerComparisonStrategy?,
        labels: EventLabels = EventLabels()
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        OneToOneMatchPayload(
            createdAt = createdAt,
            eventVersion = EVENT_VERSION,
            endedAt = endTime,
            candidateId = candidateId,
            matcher = matcher,
            result = result,
            fingerComparisonStrategy = fingerComparisonStrategy
        ),
        ONE_TO_ONE_MATCH
    )

    override fun getTokenizedFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    data class OneToOneMatchPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        override var endedAt: Long,
        val candidateId: String,
        val matcher: String,
        val result: MatchEntry?,
        val fingerComparisonStrategy: FingerComparisonStrategy?,
        override val type: EventType = ONE_TO_ONE_MATCH
    ) : EventPayload()

    companion object {
        const val EVENT_VERSION = 2
    }
}
