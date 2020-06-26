package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.EnrolmentEvent
import com.simprints.id.data.db.event.domain.events.EnrolmentEvent.EnrolmentPayload

@Keep
class ApiEnrolmentEvent(val relativeStartTime: Long,
                        val personId: String) : ApiEvent(ApiEventType.ENROLMENT) {

    constructor(enrolmentEvent: EnrolmentEvent) :
        this((enrolmentEvent.payload as EnrolmentPayload).creationTime, enrolmentEvent.payload.personId)
}
