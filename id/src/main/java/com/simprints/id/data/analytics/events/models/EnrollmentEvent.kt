package com.simprints.id.data.analytics.events.models

class EnrollmentEvent(val relativeStartTime: Long, val personId: String): Event(EventType.ENROLLMENT)
