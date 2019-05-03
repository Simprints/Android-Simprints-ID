package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.EnrolResponseEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.IdentifyResponseEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.RefusalFormResponseEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.VerifyResponseEvent

@Keep
class ApiCallout {

    constructor()
    constructor(response: EnrolResponseEvent) //StopShip: implement transformation from AppResponse to Callback
    constructor(response: IdentifyResponseEvent)
    constructor(response: VerifyResponseEvent)
    constructor(response: RefusalFormResponseEvent)
}
