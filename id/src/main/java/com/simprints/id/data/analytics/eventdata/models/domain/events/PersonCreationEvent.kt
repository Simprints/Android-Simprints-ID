package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep

// At the end of the sequence of capture, we build a Person object used either for enrolment or verification/identification
@Keep
class PersonCreationEvent(override val starTime: Long,
                          val fingerprintCaptureIds: List<String>) : Event(EventType.PERSON_CREATION)
