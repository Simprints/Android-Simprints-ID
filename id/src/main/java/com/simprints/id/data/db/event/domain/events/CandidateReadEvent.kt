package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import java.util.*

@Keep
class CandidateReadEvent(
    startTime: Long,
    endTime: Long,
    candidateId: String,
    localResult: CandidateReadPayload.LocalResult,
    remoteResult: CandidateReadPayload.RemoteResult?,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    DEFAULT_EVENT_VERSION,
    listOf(EventLabel.SessionId(sessionId)),
    CandidateReadPayload(startTime, DEFAULT_EVENT_VERSION, endTime, candidateId, localResult, remoteResult)) {


    @Keep
    class CandidateReadPayload(
        creationTime: Long,
        version: Int,
        val endTime: Long,
        val candidateId: String,
        val localResult: LocalResult,
        val remoteResult: RemoteResult?
    ) : EventPayload(EventPayloadType.CANDIDATE_READ, version, creationTime) {

        @Keep
        enum class LocalResult {
            FOUND, NOT_FOUND
        }

        @Keep
        enum class RemoteResult {
            FOUND, NOT_FOUND
        }
    }
}
