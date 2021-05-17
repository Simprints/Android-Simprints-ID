package com.simprints.eventsystem.event.remote.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.id.data.db.event.domain.models.CandidateReadEvent.CandidateReadPayload
import com.simprints.id.data.db.event.domain.models.CandidateReadEvent.CandidateReadPayload.LocalResult
import com.simprints.id.data.db.event.domain.models.CandidateReadEvent.CandidateReadPayload.RemoteResult
import com.simprints.id.data.db.event.remote.models.ApiCandidateReadPayload.ApiLocalResult
import com.simprints.id.data.db.event.remote.models.ApiCandidateReadPayload.ApiRemoteResult
import com.simprints.id.data.db.event.remote.models.ApiEventPayloadType.CandidateRead

@Keep
@JsonInclude(Include.NON_NULL)
data class ApiCandidateReadPayload(override val startTime: Long,
                                   override val version: Int,
                                   val endTime: Long,
                                   val candidateId: String,
                                   val localResult: ApiLocalResult,
                                   val remoteResult: ApiRemoteResult?) : ApiEventPayload(CandidateRead, version, startTime) {

    @Keep
    enum class ApiLocalResult {
        FOUND, NOT_FOUND;
    }

    @Keep
    enum class ApiRemoteResult {
        FOUND, NOT_FOUND;
    }

    constructor(domainPayload: CandidateReadPayload):
        this(domainPayload.createdAt,
            domainPayload.eventVersion,
            domainPayload.endedAt,
            domainPayload.candidateId,
            domainPayload.localResult.fromDomainToApi(),
            domainPayload.remoteResult?.fromDomainToApi())
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


