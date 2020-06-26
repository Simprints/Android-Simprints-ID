package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.PersonCreationEvent
import com.simprints.id.data.db.event.domain.events.PersonCreationEvent.PersonCreationPayload

// At the end of the sequence of capture, we build a Person object used either for enrolment or verification/identification
@Keep
class ApiPersonCreationEvent(val relativeStartTime: Long,
                             val fingerprintCaptureIds: List<String>) : ApiEvent(ApiEventType.PERSON_CREATION) {

    constructor(personCreationEvent: PersonCreationEvent) :
        this((personCreationEvent.payload as PersonCreationPayload).creationTime,
            personCreationEvent.payload.fingerprintCaptureIds)
}

