package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.CandidateReadEvent

@Keep
class ApiCandidateReadEvent(val relativeStartTime: Long,
                            val relativeEndTime: Long,
                            val candidateId: String,
                            val localResult: ApiLocalResult,
                            val remoteResult: ApiRemoteResult?) : ApiEvent(ApiEventType.CANDIDATE_READ) {

    @Keep
    enum class ApiLocalResult {
        FOUND, NOT_FOUND
    }

    @Keep
    enum class ApiRemoteResult {
        FOUND, NOT_FOUND
    }

    constructor(candidateReadEvent: CandidateReadEvent) :
        this(candidateReadEvent.relativeStartTime ?: 0,
            candidateReadEvent.relativeEndTime ?: 0,
            candidateReadEvent.candidateId,
            ApiLocalResult.valueOf(candidateReadEvent.localResult.toString()),
            ApiRemoteResult.valueOf(candidateReadEvent.remoteResult.toString()))
}


