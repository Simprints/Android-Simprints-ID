package com.simprints.id.data.analytics.eventdata.models.remote.events

import com.simprints.id.data.analytics.eventdata.models.domain.events.EnrolResponseEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.IdentifyResponseEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.VerifyResponseEvent

class ApiCallbackEvent(val relativeStartTime: Long,
                       val result: ApiCallout) : ApiEvent(ApiEventType.CALLBACK) {

    class ApiCallout {
        constructor(response: EnrolResponseEvent) //StopShip: implement transformation from Response to Callback
        constructor(response: IdentifyResponseEvent)
        constructor(response: VerifyResponseEvent)
    }

    constructor(enrolResponseEvent: EnrolResponseEvent) :
        this(enrolResponseEvent.relativeStartTime, ApiCallout(enrolResponseEvent))

    constructor(identifyResponseEvent: IdentifyResponseEvent) :
        this(identifyResponseEvent.relativeStartTime, ApiCallout(identifyResponseEvent))

    constructor(verifyResponseEvent: VerifyResponseEvent) :
        this(verifyResponseEvent.relativeStartTime, ApiCallout(verifyResponseEvent))

}


