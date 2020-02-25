package com.simprints.id.data.db.session.domain.events

import androidx.annotation.Keep

// At the end of the sequence of capture, we build a Person object used either for enrolment or verification/identification
@Keep
class PersonCreationEvent(starTime: Long,
                          val fingerprintCaptureIds: List<String>) : Event(EventType.PERSON_CREATION, starTime)
