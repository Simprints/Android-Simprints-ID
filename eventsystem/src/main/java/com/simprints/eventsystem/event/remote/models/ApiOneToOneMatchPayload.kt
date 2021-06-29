package com.simprints.eventsystem.event.remote.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.eventsystem.event.domain.models.OneToOneMatchEvent.OneToOneMatchPayload
import com.simprints.eventsystem.event.remote.models.face.ApiMatcher
import com.simprints.eventsystem.event.remote.models.face.fromDomainToApi

@Keep
@JsonInclude(Include.NON_NULL)
data class ApiOneToOneMatchPayload(override val startTime: Long,
                                   override val version: Int,
                                   val endTime: Long,
                                   val candidateId: String,
                                   val matcher: ApiMatcher,
                                   val result: ApiMatchEntry?) : ApiEventPayload(ApiEventPayloadType.OneToOneMatch, version, startTime) {

    constructor(domainPayload: OneToOneMatchPayload) :
        this(domainPayload.createdAt,
            domainPayload.eventVersion,
            domainPayload.endedAt,
            domainPayload.candidateId,
            domainPayload.matcher.fromDomainToApi(),
            domainPayload.result?.let { ApiMatchEntry(it) })
}

