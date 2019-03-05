package com.simprints.id.data.analytics.eventdata.models.remote.events

import com.simprints.id.data.analytics.eventdata.models.domain.events.EnrollmentEvent

class ApiEnrollmentEvent(val relativeStartTime: Long, val personId: String) : ApiEvent(ApiEventType.ENROLLMENT) {

    constructor(enrollmentEvent: EnrollmentEvent) :
        this(enrollmentEvent.relativeStartTime, enrollmentEvent.personId)
}
