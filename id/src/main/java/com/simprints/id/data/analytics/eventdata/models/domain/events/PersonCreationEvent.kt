package com.simprints.id.data.analytics.eventdata.models.domain.events

import com.simprints.id.data.analytics.eventdata.models.domain.EventType

// At the end of the sequence of capture, we build a Person object used either for enrolment or verification/identification
class PersonCreationEvent(val relativeStartTime: Long,
                          val fingerprintCaptureIds: List<String>) : Event(EventType.PERSON_CREATION)
