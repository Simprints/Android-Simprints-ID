package com.simprints.id.data.analytics.eventdata.models.domain.events

import com.simprints.id.data.analytics.eventdata.models.domain.EventType
import com.simprints.id.domain.requests.Request

class CalloutEvent(val relativeStartTime: Long,
                   val appRequest: Request) : Event(EventType.CALLOUT) //STOPSHIP: changed Callout in the event. PT?
