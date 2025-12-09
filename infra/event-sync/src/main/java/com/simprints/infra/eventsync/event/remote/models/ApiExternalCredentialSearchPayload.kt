package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.ExternalCredentialSearchEvent

@Keep
internal data class ApiExternalCredentialSearchPayload(
    override val startTime: ApiTimestamp,
    val endTime: ApiTimestamp?,
    val id: String,
    val probeExternalCredentialId: String,
    val result: ApiExternalCredentialSearchResult,
) : ApiEventPayload(startTime) {
    constructor(domainPayload: ExternalCredentialSearchEvent.ExternalCredentialSearchPayload) : this(
        startTime = domainPayload.startTime.fromDomainToApi(),
        endTime = domainPayload.endTime?.fromDomainToApi(),
        id = domainPayload.id,
        probeExternalCredentialId = domainPayload.probeExternalCredentialId,
        result = ApiExternalCredentialSearchResult(
            candidateIds = domainPayload.result.candidateIds,
        ),
    )

    @Keep
    data class ApiExternalCredentialSearchResult(
        val candidateIds: List<String>,
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null
}
