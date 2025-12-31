package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.AgeGroupSelectionEvent
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiAgeGroupSelectionPayload(
    override val startTime: ApiTimestamp,
    val endTime: ApiTimestamp,
    val subjectAgeGroup: ApiAgeGroup,
) : ApiEventPayload() {
    constructor(domainPayload: AgeGroupSelectionEvent.AgeGroupSelectionPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.endedAt?.fromDomainToApi() ?: ApiTimestamp(0),
        domainPayload.subjectAgeGroup.fromDomainToApi(),
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null // this payload doesn't have tokenizable fields

    @Keep
    @Serializable
    data class ApiAgeGroup(
        val startInclusive: Int,
        val endExclusive: Int?,
    )
}

private fun AgeGroupSelectionEvent.AgeGroup.fromDomainToApi() = ApiAgeGroupSelectionPayload.ApiAgeGroup(
    startInclusive = startInclusive,
    endExclusive = endExclusive,
)
