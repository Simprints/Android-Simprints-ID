package com.simprints.id.data.analytics.eventdata.models.domain.events

import com.simprints.id.domain.requests.EnrolRequest

class EnrolRequestEvent(val relativeStartTime: Long,
                        val enrolRequest: EnrolRequest) : Event(EventType.ENROL_REQUEST)
