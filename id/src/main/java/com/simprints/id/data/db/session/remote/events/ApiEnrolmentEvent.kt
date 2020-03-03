package com.simprints.id.data.db.session.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.EnrolmentEvent

@Keep
class ApiEnrolmentEvent(val relativeStartTime: Long,
                        val personId: String) : ApiEvent(ApiEventType.ENROLMENT) {

    constructor(enrolmentEvent: EnrolmentEvent) :
        this(enrolmentEvent.relativeStartTime ?: 0, enrolmentEvent.personId)
}
