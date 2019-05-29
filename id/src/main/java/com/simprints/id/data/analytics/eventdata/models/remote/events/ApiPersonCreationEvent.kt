package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.PersonCreationEvent

// At the end of the sequence of capture, we build a Person object used either for enrolment or verification/identification
@Keep
class ApiPersonCreationEvent(val relativeStartTime: Long,
                          val fingerprintCaptureIds: List<String>): ApiEvent(ApiEventType.PERSON_CREATION) {

    constructor(personCreationEvent: PersonCreationEvent) :
        this(personCreationEvent.relativeStartTime ?: 0, personCreationEvent.fingerprintCaptureIds)
}

