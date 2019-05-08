package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.EnrolmentEvent

@Keep
class ApiEnrollmentEvent(val relativeStartTime: Long, val personId: String) : ApiEvent(ApiEventType.ENROLMENT) {

    constructor(enrolmentEvent: EnrolmentEvent) :
        this(enrolmentEvent.relativeStartTime, enrolmentEvent.personId)
}
