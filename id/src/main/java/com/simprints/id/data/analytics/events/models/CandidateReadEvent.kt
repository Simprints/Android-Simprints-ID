package com.simprints.id.data.analytics.events.models

class CandidateReadEvent(val relativeStartTime: Long,
                         val relativeEndTime: Long,
                         val candidateId: String,
                         val localResult: LocalResult,
                         val remoteResult: RemoteResult?) : Event(EventType.CANDIDATE_READ) {

    enum class LocalResult {
        FOUND, NOT_FOUND
    }

    enum class RemoteResult {
        FOUND, NOT_FOUND, OFFLINE
    }
}
