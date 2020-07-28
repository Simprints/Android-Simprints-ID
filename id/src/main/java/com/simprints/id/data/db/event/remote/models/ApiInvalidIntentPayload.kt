package com.simprints.id.data.db.event.remote.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.InvalidIntentEvent.InvalidIntentPayload

@Keep
class ApiInvalidIntentPayload(createdAt: Long,
                              eventVersion: Int,
                              val action: String,
                              val extras: Map<String, Any?>) : ApiEventPayload(ApiEventPayloadType.INVALID_INTENT, eventVersion, createdAt) {

    constructor(domainPayload: InvalidIntentPayload) :
        this(domainPayload.createdAt,
            domainPayload.eventVersion,
            domainPayload.action,
            domainPayload.extras)
}
