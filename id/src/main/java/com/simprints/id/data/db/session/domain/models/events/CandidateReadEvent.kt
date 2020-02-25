package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep

@Keep
class CandidateReadEvent(starTime: Long,
                         endTime: Long,
                         val candidateId: String,
                         val localResult: LocalResult,
                         val remoteResult: RemoteResult?) : Event(EventType.CANDIDATE_READ, starTime, endTime) {

    @Keep
    enum class LocalResult {
        FOUND, NOT_FOUND
    }

    @Keep
    enum class RemoteResult {
        FOUND, NOT_FOUND
    }
}
