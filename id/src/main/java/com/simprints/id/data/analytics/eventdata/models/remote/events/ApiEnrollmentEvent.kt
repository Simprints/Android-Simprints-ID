package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.EnrollmentEvent

@Keep
class ApiEnrollmentEvent(val relativeStartTime: Long, val personId: String) : ApiEvent(ApiEventType.ENROLLMENT) {

    constructor(enrollmentEvent: EnrollmentEvent) :
        this(enrollmentEvent.relativeStartTime, enrollmentEvent.personId)
}
