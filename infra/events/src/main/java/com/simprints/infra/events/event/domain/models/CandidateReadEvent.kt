package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizedString
import com.simprints.infra.config.domain.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.CANDIDATE_READ
import java.util.UUID

@Keep
data class CandidateReadEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: CandidateReadPayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        endTime: Long,
        candidateId: String,
        localResult: CandidateReadPayload.LocalResult,
        remoteResult: CandidateReadPayload.RemoteResult?,
        labels: EventLabels = EventLabels()
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        CandidateReadPayload(
            createdAt = createdAt,
            eventVersion = EVENT_VERSION,
            endedAt = endTime,
            candidateId = candidateId,
            localResult = localResult,
            remoteResult = remoteResult
        ),
        CANDIDATE_READ
    )

    override fun getTokenizedFields(): Map<TokenKeyType, TokenizedString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizedString>) = this // No tokenized fields

    @Keep
    data class CandidateReadPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        override var endedAt: Long,
        val candidateId: String,
        val localResult: LocalResult,
        val remoteResult: RemoteResult?,
        override val type: EventType = CANDIDATE_READ
    ) : EventPayload() {

        @Keep
        enum class LocalResult {
            FOUND, NOT_FOUND
        }

        @Keep
        enum class RemoteResult {
            FOUND, NOT_FOUND
        }
    }

    companion object {
        const val EVENT_VERSION = 1
    }
}
