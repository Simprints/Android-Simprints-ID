package com.simprints.id.data.analytics.eventdata.models.domain.events

import com.simprints.id.data.analytics.eventdata.models.domain.EventType

class EnrollmentEvent(val relativeStartTime: Long, val personId: String): Event(EventType.ENROLLMENT)
