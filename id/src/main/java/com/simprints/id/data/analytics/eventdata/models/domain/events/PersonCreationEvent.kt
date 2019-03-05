package com.simprints.id.data.analytics.eventdata.models.domain.events

// At the end of the sequence of capture, we build a Person object used either for enrolment or verification/identification
class PersonCreationEvent(val relativeStartTime: Long,
                          val fingerprintCaptureIds: List<String>) : Event(EventType.PERSON_CREATION)
