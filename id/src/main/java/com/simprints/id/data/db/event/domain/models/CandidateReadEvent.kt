package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.EventType.CANDIDATE_READ
import com.simprints.id.data.db.event.local.models.DbEvent.Companion.DEFAULT_EVENT_VERSION
import java.util.*

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
        labels: EventLabels = EventLabels() //StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        CandidateReadPayload(createdAt, EVENT_VERSION, endTime, candidateId, localResult, remoteResult),
        CANDIDATE_READ)


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
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
