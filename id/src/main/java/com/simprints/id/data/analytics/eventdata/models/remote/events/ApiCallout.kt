package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.*

@Keep
class ApiCallout {

    constructor()
    constructor(response: EnrolResponseEvent) //StopShip: implement transformation from AppResponse to Callback
    constructor(response: IdentifyResponseEvent)
    constructor(response: VerifyResponseEvent)
    constructor(response: RefusalFormResponseEvent)
    constructor(request: EnrolRequestEvent)
    constructor(request: VerifyRequestEvent)
    constructor(request: IdentifyRequestEvent)
    constructor(request: IdentifyConfirmationRequestEvent)
}
