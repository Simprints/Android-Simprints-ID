package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest

@Keep
class EnrolRequestEvent(val relativeStartTime: Long,
                        val enrolRequest: AppEnrolRequest) : Event(EventType.ENROL_REQUEST)
