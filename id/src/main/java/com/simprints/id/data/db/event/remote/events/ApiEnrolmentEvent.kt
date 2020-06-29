package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.EnrolmentEvent
import com.simprints.id.data.db.event.domain.events.EnrolmentEvent.EnrolmentPayload

@Keep
class ApiEnrolmentEvent(domainEvent: EnrolmentEvent) :
    ApiEvent(
        domainEvent.id,
        domainEvent.labels.fromDomainToApi(),
        domainEvent.payload.fromDomainToApi()) {

    @Keep
    class ApiEnrolmentPayload(val relativeStartTime: Long,
                              val personId: String) : ApiEventPayload(ApiEventPayloadType.ENROLMENT) {

        constructor(domainPayload: EnrolmentPayload) :
            this(domainPayload.creationTime, domainPayload.personId)
    }

}
