package com.simprints.id.data.analytics.eventdata.models.domain.events

import com.simprints.id.data.analytics.eventdata.models.domain.EventType
import com.simprints.id.domain.requests.AppRequest

class CalloutEvent(val relativeStartTime: Long,
                   val appRequest: AppRequest) : Event(EventType.CALLOUT) //STOPSHIP: changed Callout in the event. PT?
