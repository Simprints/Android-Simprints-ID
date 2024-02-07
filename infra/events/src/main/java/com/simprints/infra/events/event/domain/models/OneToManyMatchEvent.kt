package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.ONE_TO_MANY_MATCH
import java.util.UUID

@Keep
data class OneToManyMatchEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: OneToManyMatchPayload,
    override val type: EventType,
    override var sessionId: String? = null,
    override var projectId: String? = null,
) : Event() {

    constructor(
        createdAt: Timestamp,
        endTime: Timestamp,
        pool: OneToManyMatchPayload.MatchPool,
        matcher: String,
        result: List<MatchEntry>?,
    ) : this(
        UUID.randomUUID().toString(),
        OneToManyMatchPayload(createdAt, EVENT_VERSION, endTime, pool, matcher, result),
        ONE_TO_MANY_MATCH
    )

    override fun getTokenizedFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) =
        this // No tokenized fields

    @Keep
    data class OneToManyMatchPayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        override val endedAt: Timestamp?,
        val pool: MatchPool,
        val matcher: String,
        val result: List<MatchEntry>?,
        override val type: EventType = ONE_TO_MANY_MATCH,
    ) : EventPayload() {

        @Keep
        data class MatchPool(val type: MatchPoolType, val count: Int)

        @Keep
        enum class MatchPoolType {

            USER,
            MODULE,
            PROJECT;
        }
    }

    companion object {

        const val EVENT_VERSION = 2
    }
}
