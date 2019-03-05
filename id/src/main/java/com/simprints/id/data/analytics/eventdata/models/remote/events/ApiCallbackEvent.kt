package com.simprints.id.data.analytics.eventdata.models.remote.events

import com.simprints.id.data.analytics.eventdata.models.domain.events.*

class ApiCallbackEvent(val relativeStartTime: Long,
                       val result: ApiCallout) : ApiEvent(ApiEventType.CALLBACK) {

    constructor(noResponseEvent: NoResponseEvent) :
        this(noResponseEvent.relativeStartTime, ApiCallout())

    constructor(enrolResponseEvent: EnrolResponseEvent) :
        this(enrolResponseEvent.relativeStartTime, ApiCallout(enrolResponseEvent))

    constructor(identifyResponseEvent: IdentifyResponseEvent) :
        this(identifyResponseEvent.relativeStartTime, ApiCallout(identifyResponseEvent))

    constructor(verifyResponseEvent: VerifyResponseEvent) :
        this(verifyResponseEvent.relativeStartTime, ApiCallout(verifyResponseEvent))

    constructor(refusalFormResponseEvent: RefusalFormResponseEvent) :
        this(refusalFormResponseEvent.relativeStartTime, ApiCallout(refusalFormResponseEvent))
}


