package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.InvalidIntentEvent
import com.simprints.id.data.db.event.domain.models.InvalidIntentEvent.InvalidIntentPayload

@Keep
class ApiInvalidIntentEvent(domainEvent: InvalidIntentEvent) :
    ApiEvent(
        domainEvent.id,
        domainEvent.labels.map { it.fromDomainToApi() }.toMap(),
        domainEvent.payload.fromDomainToApi()) {

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
}
