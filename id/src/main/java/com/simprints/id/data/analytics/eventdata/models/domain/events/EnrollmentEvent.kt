package com.simprints.id.data.analytics.eventdata.models.domain.events

class EnrollmentEvent(val relativeStartTime: Long, val personId: String): Event(EventType.ENROLLMENT)
