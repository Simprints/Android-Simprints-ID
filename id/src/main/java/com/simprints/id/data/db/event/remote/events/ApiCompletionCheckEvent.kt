package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.CompletionCheckEvent
import com.simprints.id.data.db.event.domain.events.CompletionCheckEvent.CompletionCheckPayload

@Keep
class ApiCompletionCheckEvent(domainEvent: CompletionCheckEvent) :
    ApiEvent(
        domainEvent.id,
        domainEvent.labels.fromDomainToApi(),
        domainEvent.payload.fromDomainToApi()) {

    @Keep
    class ApiCompletionCheckPayload(val relativeStartTime: Long,
                                    val completed: Boolean) : ApiEventPayload(ApiEventPayloadType.COMPLETION_CHECK) {

        constructor(domainPayload: CompletionCheckPayload) :
            this(domainPayload.creationTime, domainPayload.completed)
    }
}
