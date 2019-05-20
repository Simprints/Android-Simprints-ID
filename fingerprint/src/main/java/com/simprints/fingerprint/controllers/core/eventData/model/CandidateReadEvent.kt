package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.CandidateReadEvent as CoreCandidateReadEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.CandidateReadEvent.LocalResult as CoreLocalResult
import com.simprints.id.data.analytics.eventdata.models.domain.events.CandidateReadEvent.RemoteResult as CoreRemoteResult

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

fun CandidateReadEvent.fromDomainToCore() =
    CoreCandidateReadEvent(starTime, endTime, candidateId, localResult.fromDomainToCore(), remoteResult?.fromDomainToCore() )


fun CandidateReadEvent.LocalResult.fromDomainToCore() =
    when(this) {
        CandidateReadEvent.LocalResult.FOUND -> CoreLocalResult.FOUND
        CandidateReadEvent.LocalResult.NOT_FOUND -> CoreLocalResult.NOT_FOUND
    }

fun CandidateReadEvent.RemoteResult.fromDomainToCore() =
    when(this) {
        CandidateReadEvent.RemoteResult.FOUND -> CoreRemoteResult.FOUND
        CandidateReadEvent.RemoteResult.NOT_FOUND -> CoreRemoteResult.NOT_FOUND
    }
