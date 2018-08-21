package com.simprints.id.data.analytics.eventData.models.events

import com.simprints.id.data.analytics.eventData.models.EventType

class EnrollmentEvent(val relativeStartTime: Long, val personId: String): Event(EventType.ENROLLMENT)
