package com.simprints.id.data.analytics.eventdata.models.remote.events

import com.simprints.id.data.analytics.eventdata.models.domain.events.CandidateReadEvent

class ApiCandidateReadEvent(val relativeStartTime: Long,
                            val relativeEndTime: Long,
                            val candidateId: String,
                            val localResult: ApiLocalResult,
                            val remoteResult: ApiRemoteResult?) : ApiEvent(ApiEventType.CANDIDATE_READ) {

    enum class ApiLocalResult {
        FOUND, NOT_FOUND
    }

    enum class ApiRemoteResult {
        FOUND, NOT_FOUND
    }

    constructor(candidateReadEvent: CandidateReadEvent) :
        this(candidateReadEvent.relativeStartTime,
            candidateReadEvent.relativeEndTime,
            candidateReadEvent.candidateId,
            ApiLocalResult.valueOf(candidateReadEvent.localResult.toString()),
            ApiRemoteResult.valueOf(candidateReadEvent.remoteResult.toString()))
}


