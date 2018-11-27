package com.simprints.id.data.analytics.eventData.models.domain.events

import com.simprints.id.data.analytics.eventData.models.domain.EventType

class EnrollmentEvent(val relativeStartTime: Long, val personId: String): Event(EventType.ENROLLMENT)
