package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep

@Keep
class EnrolmentEvent(val relativeStartTime: Long, val personId: String): Event(EventType.ENROLMENT)
