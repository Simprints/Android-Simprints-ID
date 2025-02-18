package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.CANDIDATE_READ
import java.util.UUID

@Keep
data class CandidateReadEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: CandidateReadPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        createdAt: Timestamp,
        endTime: Timestamp,
        candidateId: String,
        localResult: CandidateReadPayload.LocalResult,
        remoteResult: CandidateReadPayload.RemoteResult?,
    ) : this(
        UUID.randomUUID().toString(),
        CandidateReadPayload(
            createdAt = createdAt,
            eventVersion = EVENT_VERSION,
            endedAt = endTime,
            candidateId = candidateId,
            localResult = localResult,
            remoteResult = remoteResult,
        ),
        CANDIDATE_READ,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    data class CandidateReadPayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        val candidateId: String,
        val localResult: LocalResult,
        val remoteResult: RemoteResult?,
        override val endedAt: Timestamp?,
        override val type: EventType = CANDIDATE_READ,
    ) : EventPayload() {
        override fun toSafeString(): String = "guid: $candidateId, local: $localResult, remote: $remoteResult"

        @Keep
        enum class LocalResult {
            FOUND,
            NOT_FOUND,
        }

        @Keep
        enum class RemoteResult {
            FOUND,
            NOT_FOUND,
        }
    }

    companion object {
        const val EVENT_VERSION = 2
    }
}
