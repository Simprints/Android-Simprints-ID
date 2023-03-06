package com.simprints.infra.events.remote.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.infra.events.event.domain.models.InvalidIntentEvent.InvalidIntentPayload

@Keep
@JsonInclude(Include.NON_NULL)
data class ApiInvalidIntentPayload(override val startTime: Long,
                                   override val version: Int,
                                   val action: String,
                                   val extras: Map<String, Any?>) : ApiEventPayload(ApiEventPayloadType.InvalidIntent, version, startTime) {

    constructor(domainPayload: InvalidIntentPayload) :
        this(domainPayload.createdAt,
            domainPayload.eventVersion,
            domainPayload.action,
            domainPayload.extras)
}
