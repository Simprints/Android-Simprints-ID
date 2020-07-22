package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.CandidateReadEvent
import com.simprints.id.data.db.event.domain.models.CandidateReadEvent.CandidateReadPayload
import com.simprints.id.data.db.event.domain.models.CandidateReadEvent.CandidateReadPayload.LocalResult
import com.simprints.id.data.db.event.domain.models.CandidateReadEvent.CandidateReadPayload.RemoteResult
import com.simprints.id.data.db.event.remote.events.ApiCandidateReadEvent.ApiCandidateReadPayload.ApiLocalResult
import com.simprints.id.data.db.event.remote.events.ApiCandidateReadEvent.ApiCandidateReadPayload.ApiRemoteResult
import com.simprints.id.data.db.event.remote.events.ApiEventPayloadType.CANDIDATE_READ

@Keep
class ApiCandidateReadEvent(domainEvent: CandidateReadEvent) :
    ApiEvent(
        domainEvent.id,
        domainEvent.labels.map { it.fromDomainToApi() }.toMap(),
        domainEvent.payload.fromDomainToApi()) {

    @Keep
    class ApiCandidateReadPayload(createdAt: Long,
                                  eventVersion: Int,
                                  val endedAt: Long,
                                  val candidateId: String,
                                  val localResult: ApiLocalResult,
                                  val remoteResult: ApiRemoteResult?) : ApiEventPayload(CANDIDATE_READ, eventVersion, createdAt) {

        @Keep
        enum class ApiLocalResult {
            FOUND, NOT_FOUND;
        }

        @Keep
        enum class ApiRemoteResult {
            FOUND, NOT_FOUND;
        }

        constructor(domainPayload: CandidateReadPayload) :
            this(domainPayload.createdAt,
                domainPayload.eventVersion,
                domainPayload.endedAt,
                domainPayload.candidateId,
                domainPayload.localResult.fromDomainToApi(),
                domainPayload.remoteResult?.fromDomainToApi())
    }
}

fun LocalResult.fromDomainToApi() =
    when (this) {
        LocalResult.FOUND -> ApiLocalResult.FOUND
        LocalResult.NOT_FOUND -> ApiLocalResult.NOT_FOUND
    }

fun RemoteResult.fromDomainToApi() =
    when (this) {
        RemoteResult.FOUND -> ApiRemoteResult.FOUND
        RemoteResult.NOT_FOUND -> ApiRemoteResult.NOT_FOUND
    }


