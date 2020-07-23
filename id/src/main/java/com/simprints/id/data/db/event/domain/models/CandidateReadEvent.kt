package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.CANDIDATE_READ
import java.util.*

@Keep
class CandidateReadEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val labels: MutableList<EventLabel>,
    override val payload: CandidateReadPayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(
        createdAt: Long,
        endTime: Long,
        candidateId: String,
        localResult: CandidateReadPayload.LocalResult,
        remoteResult: CandidateReadPayload.RemoteResult?,
        sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        mutableListOf(SessionIdLabel(sessionId)),
        CandidateReadPayload(createdAt, EVENT_VERSION, endTime, candidateId, localResult, remoteResult),
        CANDIDATE_READ)


    @Keep
    class CandidateReadPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        override val endedAt: Long,
        val candidateId: String,
        val localResult: LocalResult,
        val remoteResult: RemoteResult?
    ) : EventPayload(CANDIDATE_READ, eventVersion, createdAt, endedAt) {

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
