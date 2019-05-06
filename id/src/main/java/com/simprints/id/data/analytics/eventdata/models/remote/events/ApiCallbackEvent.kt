package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.*

@Keep
class ApiCallbackEvent(val relativeStartTime: Long,
                       val result: ApiCallout? = null) : ApiEvent(ApiEventType.CALLBACK) {

    constructor(noResponseEvent: NoResponseEvent) :
        this(noResponseEvent.relativeStartTime)

    constructor(enrolResponseEvent: EnrolResponseEvent) :
        this(enrolResponseEvent.relativeStartTime)

    constructor(identifyResponseEvent: IdentifyResponseEvent) :
        this(identifyResponseEvent.relativeStartTime)

    constructor(verifyResponseEvent: VerifyResponseEvent) :
        this(verifyResponseEvent.relativeStartTime)

    constructor(refusalFormResponseEvent: RefusalFormResponseEvent) :
        this(refusalFormResponseEvent.relativeStartTime)
}


