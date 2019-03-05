package com.simprints.id.data.analytics.eventdata.models.remote.events

import com.simprints.id.data.analytics.eventdata.models.domain.events.*

class ApiCallout {
    constructor(response: EnrolResponseEvent) //StopShip: implement transformation from Response to Callback
    constructor(response: IdentifyResponseEvent)
    constructor(response: VerifyResponseEvent)
    constructor(request: EnrolRequestEvent)
    constructor(request: VerifyRequestEvent)
    constructor(request: IdentifyRequestEvent)
    constructor(request: IdentifyConfirmationRequestEvent)
}
