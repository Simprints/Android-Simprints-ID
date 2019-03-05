package com.simprints.id.data.analytics.eventdata.models.remote.events

import com.simprints.id.data.analytics.eventdata.models.domain.events.EnrolRequestEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.IdentifyConfirmationRequestEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.IdentifyRequestEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.VerifyRequestEvent


class ApiCalloutEvent(val relativeStartTime: Long,
                      val parameters: ApiCallout) : ApiEvent(ApiEventType.CALLOUT) {

    constructor(enrolRequestEvent: EnrolRequestEvent) :
        this(enrolRequestEvent.relativeStartTime, ApiCallout(enrolRequestEvent))

    constructor(identifyRequestEvent: IdentifyRequestEvent) :
        this(identifyRequestEvent.relativeStartTime, ApiCallout(identifyRequestEvent))

    constructor(verifyRequestEvent: VerifyRequestEvent) :
        this(verifyRequestEvent.relativeStartTime, ApiCallout(verifyRequestEvent))

    constructor(identifyConfirmationRequestEvent: IdentifyConfirmationRequestEvent) :
        this(identifyConfirmationRequestEvent.relativeStartTime, ApiCallout(identifyConfirmationRequestEvent))

}
