package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep

@Keep
class EnrolmentEvent(override val starTime: Long,
                     val personId: String): Event(EventType.ENROLMENT)
