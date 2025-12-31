package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.CandidateReadEvent.CandidateReadPayload
import com.simprints.infra.events.event.domain.models.CandidateReadEvent.CandidateReadPayload.LocalResult
import com.simprints.infra.events.event.domain.models.CandidateReadEvent.CandidateReadPayload.RemoteResult
import com.simprints.infra.eventsync.event.remote.models.ApiCandidateReadPayload.ApiLocalResult
import com.simprints.infra.eventsync.event.remote.models.ApiCandidateReadPayload.ApiRemoteResult
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiCandidateReadPayload(
    override val startTime: ApiTimestamp,
    val endTime: ApiTimestamp?,
    val candidateId: String,
    val localResult: ApiLocalResult,
    val remoteResult: ApiRemoteResult?,
) : ApiEventPayload() {
    @Keep
    @Serializable
    enum class ApiLocalResult {
        FOUND,
        NOT_FOUND,
    }

    @Keep
    @Serializable
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
