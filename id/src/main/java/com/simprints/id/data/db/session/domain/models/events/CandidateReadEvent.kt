package com.simprints.id.data.db.session.domain.models.events

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
    listOf(EventLabel.SessionId(sessionId)),
    CandidateReadPayload(startTime, endTime, candidateId, localResult, remoteResult)) {


    @Keep
    class CandidateReadPayload(
        val startTime: Long,
        val endTime: Long,
        val candidateId: String,
        val localResult: LocalResult,
        val remoteResult: RemoteResult?
    ) : EventPayload(EventPayloadType.CANDIDATE_READ) {

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
