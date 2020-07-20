package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.PersonCreationEvent
import com.simprints.id.data.db.event.domain.models.PersonCreationEvent.PersonCreationPayload

// At the end of the sequence of capture, we build a Person object used either for enrolment or verification/identification
@Keep
class ApiPersonCreationEvent(domainEvent: PersonCreationEvent) :
    ApiEvent(
        domainEvent.id,
        domainEvent.labels.map { it.fromDomainToApi() }.toMap(),
        domainEvent.payload.fromDomainToApi()) {

    @Keep
    class ApiPersonCreationPayload(createdAt: Long,
                                   eventVersion: Int,
                                   val fingerprintCaptureIds: List<String>) : ApiEventPayload(ApiEventPayloadType.PERSON_CREATION, eventVersion, createdAt) {

        constructor(domainPayload: PersonCreationPayload) :
            this(domainPayload.createdAt,
                domainPayload.eventVersion,
                domainPayload.fingerprintCaptureIds)
    }
}

