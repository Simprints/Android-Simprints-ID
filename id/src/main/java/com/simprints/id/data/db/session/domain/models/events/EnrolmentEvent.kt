package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep

@Keep
class EnrolmentEvent(startTime: Long,
                     val personId: String): Event(EventType.ENROLMENT, startTime)
