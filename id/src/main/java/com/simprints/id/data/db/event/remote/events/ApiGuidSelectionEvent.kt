package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.GuidSelectionEvent
import com.simprints.id.data.db.event.domain.events.GuidSelectionEvent.GuidSelectionPayload

@Keep
class ApiGuidSelectionEvent(domainEvent: GuidSelectionEvent) :
    ApiEvent(
        domainEvent.id,
        domainEvent.labels.fromDomainToApi(),
        domainEvent.payload.fromDomainToApi()) {


    @Keep
    class ApiGuidSelectionPayload(val relativeStartTime: Long,
                                  val selectedId: String) : ApiEventPayload(ApiEventPayloadType.GUID_SELECTION) {

        constructor(domainPayload: GuidSelectionPayload) :
            this(domainPayload.creationTime,
                domainPayload.selectedId)
    }
}
