package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.OneToOneMatchEvent.OneToOneMatchPayload

@Keep
@JsonInclude(Include.NON_NULL)
internal data class ApiOneToOneMatchPayload(
    override val startTime: ApiTimestamp,
    val endTime: ApiTimestamp?,
    val candidateId: String,
    val matcher: String,
    val result: ApiMatchEntry?,
    val fingerComparisonStrategy: ApiFingerComparisonStrategy?,
) : ApiEventPayload(startTime) {
    constructor(domainPayload: OneToOneMatchPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.endedAt?.fromDomainToApi(),
        domainPayload.candidateId,
        domainPayload.matcher,
        domainPayload.result?.let { ApiMatchEntry(it) },
        domainPayload.fingerComparisonStrategy?.fromDomainToApi(),
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null // this payload doesn't have tokenizable fields
}
