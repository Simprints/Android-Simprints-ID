package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.InvalidIntentEvent
import com.simprints.id.data.db.event.domain.events.InvalidIntentEvent.InvalidIntentPayload

@Keep
class ApiInvalidIntentEvent(domainEvent: InvalidIntentEvent) :
    ApiEvent(
        domainEvent.id,
        domainEvent.labels.fromDomainToApi(),
        domainEvent.payload.fromDomainToApi()) {

    @Keep
    class ApiInvalidIntentPayload(val relativeStartTime: Long,
                                  val action: String,
                                  val extras: Map<String, Any?>) : ApiEventPayload(ApiEventPayloadType.INVALID_INTENT) {

        constructor(domainPayload: InvalidIntentPayload) :
            this(domainPayload.creationTime,
                domainPayload.action,
                domainPayload.extras)
    }
}
