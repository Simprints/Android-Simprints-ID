package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.ONE_TO_ONE_MATCH
import java.util.UUID

@Keep
data class OneToOneMatchEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: OneToOneMatchPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        createdAt: Timestamp,
        endTime: Timestamp,
        candidateId: String,
        matcher: String,
        result: MatchEntry?,
        fingerComparisonStrategy: FingerComparisonStrategy?,
    ) : this(
        UUID.randomUUID().toString(),
        OneToOneMatchPayload(
            createdAt = createdAt,
            eventVersion = EVENT_VERSION,
            endedAt = endTime,
            candidateId = candidateId,
            matcher = matcher,
            result = result,
            fingerComparisonStrategy = fingerComparisonStrategy,
        ),
        ONE_TO_ONE_MATCH,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    data class OneToOneMatchPayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        override var endedAt: Timestamp?,
        val candidateId: String,
        val matcher: String,
        val result: MatchEntry?,
        val fingerComparisonStrategy: FingerComparisonStrategy?,
        override val type: EventType = ONE_TO_ONE_MATCH,
    ) : EventPayload() {
        override fun toSafeString(): String = "matcher: $matcher, candidate ID: $candidateId, " +
            "result: ${result?.score}, finger strategy: $fingerComparisonStrategy"
    }

    companion object {
        const val EVENT_VERSION = 3
    }
}
