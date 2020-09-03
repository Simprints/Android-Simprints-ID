package com.simprints.id.data.db.event.remote.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.id.data.db.event.domain.models.InvalidIntentEvent.InvalidIntentPayload

@Keep
@JsonInclude(Include.NON_NULL)
data class ApiInvalidIntentPayload(override val relativeStartTime: Long,
                                   override val version: Int,
                                   val action: String,
                                   val extras: Map<String, Any?>) : ApiEventPayload(ApiEventPayloadType.InvalidIntent, version, relativeStartTime) {

    constructor(domainPayload: InvalidIntentPayload, baseStartTime: Long) :
        this(domainPayload.createdAt - baseStartTime,
            domainPayload.eventVersion,
            domainPayload.action,
            domainPayload.extras)
}
