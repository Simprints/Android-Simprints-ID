package com.simprints.id.data.db.event.remote.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.id.data.db.event.domain.models.SuspiciousIntentEvent.SuspiciousIntentPayload

@Keep
@JsonInclude(Include.NON_NULL)
data class ApiSuspiciousIntentPayload(override val relativeStartTime: Long,
                                      override val version: Int,
                                      val unexpectedExtras: Map<String, Any?>) : ApiEventPayload(ApiEventPayloadType.SuspiciousIntent, version, relativeStartTime) {

    constructor(domainPayload: SuspiciousIntentPayload) :
        this(domainPayload.createdAt,
            domainPayload.eventVersion,
            domainPayload.unexpectedExtras)
}

