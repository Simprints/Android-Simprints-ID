package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep
import com.simprints.id.domain.moduleapi.app.responses.AppEnrolResponse

@Keep
class EnrolResponseEvent(val relativeStartTime: Long,
                         val enrolResponse: AppEnrolResponse) : Event(EventType.ENROL_RESPONSE) //STOPSHIP: changed Callout in the event. PT?
