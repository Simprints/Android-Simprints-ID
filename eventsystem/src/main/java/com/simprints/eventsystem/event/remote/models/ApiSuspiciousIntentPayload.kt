package com.simprints.eventsystem.event.remote.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.eventsystem.event.domain.models.SuspiciousIntentEvent.SuspiciousIntentPayload

@Keep
@JsonInclude(Include.NON_NULL)
data class ApiSuspiciousIntentPayload(override val startTime: Long,
                                      override val version: Int,
                                      val unexpectedExtras: Map<String, Any?>) : ApiEventPayload(ApiEventPayloadType.SuspiciousIntent, version, startTime) {

    constructor(domainPayload: SuspiciousIntentPayload) :
        this(domainPayload.createdAt,
            domainPayload.eventVersion,
            domainPayload.unexpectedExtras)
}

