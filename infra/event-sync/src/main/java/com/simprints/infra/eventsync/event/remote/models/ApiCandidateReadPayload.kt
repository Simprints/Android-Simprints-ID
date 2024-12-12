package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.CandidateReadEvent.CandidateReadPayload
import com.simprints.infra.events.event.domain.models.CandidateReadEvent.CandidateReadPayload.LocalResult
import com.simprints.infra.events.event.domain.models.CandidateReadEvent.CandidateReadPayload.RemoteResult
import com.simprints.infra.eventsync.event.remote.models.ApiCandidateReadPayload.ApiLocalResult
import com.simprints.infra.eventsync.event.remote.models.ApiCandidateReadPayload.ApiRemoteResult

@Keep
@JsonInclude(Include.NON_NULL)
internal data class ApiCandidateReadPayload(
    override val startTime: ApiTimestamp,
    val endTime: ApiTimestamp?,
    val candidateId: String,
    val localResult: ApiLocalResult,
    val remoteResult: ApiRemoteResult?,
) : ApiEventPayload(startTime) {
    @Keep
    enum class ApiLocalResult {
        FOUND,
        NOT_FOUND,
    }

    @Keep
    enum class ApiRemoteResult {
        FOUND,
        NOT_FOUND,
    }

    constructor(domainPayload: CandidateReadPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.endedAt?.fromDomainToApi(),
        domainPayload.candidateId,
        domainPayload.localResult.fromDomainToApi(),
        domainPayload.remoteResult?.fromDomainToApi(),
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null // this payload doesn't have tokenizable fields
}

internal fun LocalResult.fromDomainToApi() = when (this) {
    LocalResult.FOUND -> ApiLocalResult.FOUND
    LocalResult.NOT_FOUND -> ApiLocalResult.NOT_FOUND
}

internal fun RemoteResult.fromDomainToApi() = when (this) {
    RemoteResult.FOUND -> ApiRemoteResult.FOUND
    RemoteResult.NOT_FOUND -> ApiRemoteResult.NOT_FOUND
}
