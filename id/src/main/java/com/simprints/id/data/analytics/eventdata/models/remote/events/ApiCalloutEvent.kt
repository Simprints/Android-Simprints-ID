package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.Callout
import com.simprints.id.data.analytics.eventdata.models.domain.events.CalloutEvent

@Keep
class ApiCalloutEvent(val relativeStartTime: Long,
                      val integration: String?,
                      val callout: Callout) : ApiEvent(ApiEventType.CALLOUT) {

    constructor(calloutEvent: CalloutEvent):
        this(calloutEvent.relativeStartTime, calloutEvent.integration, calloutEvent.callout)
}
