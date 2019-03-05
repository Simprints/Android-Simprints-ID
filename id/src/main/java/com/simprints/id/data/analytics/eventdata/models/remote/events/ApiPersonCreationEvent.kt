package com.simprints.id.data.analytics.eventdata.models.remote.events

import com.simprints.id.data.analytics.eventdata.models.domain.events.PersonCreationEvent

// At the end of the sequence of capture, we build a Person object used either for enrolment or verification/identification
class ApiPersonCreationEvent(val relativeStartTime: Long,
                          val fingerprintCaptureIds: List<String>): ApiEvent(ApiEventType.PERSON_CREATION) {

    constructor(personCreationEvent: PersonCreationEvent) :
        this(personCreationEvent.relativeStartTime, personCreationEvent.fingerprintCaptureIds)
}

