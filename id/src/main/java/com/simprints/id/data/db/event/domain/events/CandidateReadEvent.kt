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
    sessionId: String = UUID.randomUUID().toString(), //StopShip: to change in PAS-993
    sessionStartTime: Long = 0 //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    CandidateReadPayload(startTime, startTime - sessionStartTime, endTime, endTime - sessionStartTime, candidateId, localResult, remoteResult)) {


    @Keep
    class CandidateReadPayload(
        startTime: Long,
        relativeStarTime: Long,
        val endTime: Long,
        val relativeEndTime: Long,
        val candidateId: String,
        val localResult: LocalResult,
        val remoteResult: RemoteResult?
    ) : EventPayload(EventPayloadType.CANDIDATE_READ, startTime, relativeStarTime) {

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
