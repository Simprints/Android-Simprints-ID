package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep

@Keep
class EnrollmentEvent(val relativeStartTime: Long, val personId: String): Event(EventType.ENROLLMENT)
