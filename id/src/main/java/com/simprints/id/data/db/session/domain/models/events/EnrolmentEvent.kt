package com.simprints.id.data.db.session.domain.events

import androidx.annotation.Keep

@Keep
class EnrolmentEvent(starTime: Long,
                     val personId: String): Event(EventType.ENROLMENT, starTime)
