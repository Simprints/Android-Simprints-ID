package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.infra.events.event.domain.models.SuspiciousIntentEvent.SuspiciousIntentPayload
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayload
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType

@Keep
@JsonInclude(Include.NON_NULL)
data class ApiSuspiciousIntentPayload(
    override val startTime: Long,
    override val version: Int,
    val unexpectedExtras: Map<String, Any?>,
) : ApiEventPayload(ApiEventPayloadType.SuspiciousIntent, version, startTime) {

    constructor(domainPayload: SuspiciousIntentPayload) :
        this(domainPayload.createdAt,
            domainPayload.eventVersion,
            domainPayload.unexpectedExtras)
}

