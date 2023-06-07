package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.infra.events.event.domain.models.OneToOneMatchEvent.OneToOneMatchPayload

@Keep
@JsonInclude(Include.NON_NULL)
internal data class ApiOneToOneMatchPayload(
    override val startTime: Long,
    override val version: Int,
    val endTime: Long,
    val candidateId: String,
    val matcher:String,
    val result: ApiMatchEntry?,
    val fingerComparisonStrategy: ApiFingerComparisonStrategy?
) : ApiEventPayload(ApiEventPayloadType.OneToOneMatch, version, startTime) {

    constructor(domainPayload: OneToOneMatchPayload) :
        this(
            domainPayload.createdAt,
            domainPayload.eventVersion,
            domainPayload.endedAt,
            domainPayload.candidateId,
            domainPayload.matcher,
            domainPayload.result?.let { ApiMatchEntry(it) },
            domainPayload.fingerComparisonStrategy?.fromDomainToApi()
        )
}

