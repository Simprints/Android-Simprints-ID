package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep

@Keep
class EnrolmentEvent(starTime: Long,
                     val personId: String): Event(EventType.ENROLMENT, starTime)
