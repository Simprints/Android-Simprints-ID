package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.CandidateReadEvent
import com.simprints.id.data.db.event.domain.events.CandidateReadEvent.CandidateReadPayload
import com.simprints.id.data.db.event.domain.events.CandidateReadEvent.CandidateReadPayload.LocalResult
import com.simprints.id.data.db.event.domain.events.CandidateReadEvent.CandidateReadPayload.RemoteResult

@Keep
class ApiCandidateReadEvent(val relativeStartTime: Long,
                            val relativeEndTime: Long,
                            val candidateId: String,
                            val localResult: ApiLocalResult,
                            val remoteResult: ApiRemoteResult?) : ApiEvent(ApiEventType.CANDIDATE_READ) {

    @Keep
    enum class ApiLocalResult {
        FOUND, NOT_FOUND;

        companion object {
            fun fromDomainToApi(localResult: LocalResult) =
                when (localResult) {
                    LocalResult.FOUND -> FOUND
                    LocalResult.NOT_FOUND -> NOT_FOUND
                }
        }
    }

    @Keep
    enum class ApiRemoteResult {
        FOUND, NOT_FOUND;

        companion object {
            fun fromDomainToApi(remoteResult: RemoteResult?) =
                when (remoteResult) {
                    RemoteResult.FOUND -> FOUND
                    RemoteResult.NOT_FOUND -> NOT_FOUND
                    else -> null
                }
        }
    }

    constructor(candidateReadEvent: CandidateReadEvent) :
        this((candidateReadEvent.payload as CandidateReadPayload).creationTime,
            candidateReadEvent.payload.endTime,
            candidateReadEvent.payload.candidateId,
            ApiLocalResult.fromDomainToApi(candidateReadEvent.payload.localResult),
            ApiRemoteResult.fromDomainToApi(candidateReadEvent.payload.remoteResult))
}



