package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep

@Keep
class CandidateReadEvent(val relativeStartTime: Long,
                         val relativeEndTime: Long,
                         val candidateId: String,
                         val localResult: LocalResult,
                         val remoteResult: RemoteResult?) : Event(EventType.CANDIDATE_READ) {

    @Keep
    enum class LocalResult {
        FOUND, NOT_FOUND
    }

    @Keep
    enum class RemoteResult {
        FOUND, NOT_FOUND
    }
}
