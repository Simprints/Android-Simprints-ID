package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.AgeGroupSelectionEvent

@Keep
internal data class ApiAgeGroupSelectionPayload(
    override val startTime: ApiTimestamp,
    val endTime: ApiTimestamp,
    val subjectAgeGroup: ApiAgeGroup,
) : ApiEventPayload(startTime) {
    constructor(domainPayload: AgeGroupSelectionEvent.AgeGroupSelectionPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.endedAt?.fromDomainToApi() ?: ApiTimestamp(0),
        domainPayload.subjectAgeGroup.fromDomainToApi(),
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null // this payload doesn't have tokenizable fields

    data class ApiAgeGroup(
        val startInclusive: Int,
        val endExclusive: Int?,
    )
}

private fun AgeGroupSelectionEvent.AgeGroup.fromDomainToApi() = ApiAgeGroupSelectionPayload.ApiAgeGroup(
    startInclusive = startInclusive,
    endExclusive = endExclusive,
)
