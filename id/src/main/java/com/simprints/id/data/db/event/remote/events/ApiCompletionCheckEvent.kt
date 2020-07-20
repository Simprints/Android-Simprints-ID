package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.CompletionCheckEvent
import com.simprints.id.data.db.event.domain.models.CompletionCheckEvent.CompletionCheckPayload

@Keep
class ApiCompletionCheckEvent(domainEvent: CompletionCheckEvent) :
    ApiEvent(
        domainEvent.id,
        domainEvent.labels.map { it.fromDomainToApi() }.toMap(),
        domainEvent.payload.fromDomainToApi()) {

    @Keep
    class ApiCompletionCheckPayload(createdAt: Long,
                                    eventVersion: Int,
                                    val completed: Boolean) : ApiEventPayload(ApiEventPayloadType.COMPLETION_CHECK, eventVersion, createdAt) {

        constructor(domainPayload: CompletionCheckPayload) :
            this(domainPayload.createdAt, domainPayload.eventVersion, domainPayload.completed)
    }
}
