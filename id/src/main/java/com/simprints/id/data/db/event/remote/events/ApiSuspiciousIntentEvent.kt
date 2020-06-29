package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.SuspiciousIntentEvent
import com.simprints.id.data.db.event.domain.events.SuspiciousIntentEvent.SuspiciousIntentPayload

@Keep
class ApiSuspiciousIntentEvent(domainEvent: SuspiciousIntentEvent) :
    ApiEvent(
        domainEvent.id,
        domainEvent.labels.fromDomainToApi(),
        domainEvent.payload.fromDomainToApi()) {


    @Keep
    class ApiSuspiciousIntentPayload(val relativeStartTime: Long,
                                     val unexpectedExtras: Map<String, Any?>) : ApiEventPayload(ApiEventPayloadType.SUSPICIOUS_INTENT) {

        constructor(domainPayload: SuspiciousIntentPayload) :
            this(domainPayload.creationTime,
                domainPayload.unexpectedExtras)
    }
}
