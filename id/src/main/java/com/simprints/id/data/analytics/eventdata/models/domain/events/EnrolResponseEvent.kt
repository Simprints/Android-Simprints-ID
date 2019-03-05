package com.simprints.id.data.analytics.eventdata.models.domain.events

import com.simprints.id.domain.responses.EnrolResponse
import com.simprints.id.domain.responses.Response

class EnrolResponseEvent(val relativeStartTime: Long,
                         val enrolResponse: EnrolResponse) : Event(EventType.ENROL_RESPONSE) //STOPSHIP: changed Callout in the event. PT?
