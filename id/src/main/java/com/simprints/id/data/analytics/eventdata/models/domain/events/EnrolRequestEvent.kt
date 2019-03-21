package com.simprints.id.data.analytics.eventdata.models.domain.events

import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest

class EnrolRequestEvent(val relativeStartTime: Long,
                        val enrolRequest: AppEnrolRequest) : Event(EventType.ENROL_REQUEST)
