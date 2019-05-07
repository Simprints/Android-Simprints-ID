package com.simprints.clientapi.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.clientapi.exceptions.InvalidClientRequestException
import com.simprints.id.data.analytics.eventdata.models.domain.events.callout.Callout as CoreCallout
import com.simprints.id.data.analytics.eventdata.models.domain.events.CalloutEvent as CoreCalloutEvent

@Keep
class CalloutEvent(val integration: String, val relativeStartTime: Long, val callout: Callout): Event(EventType.CALLOUT)

fun CalloutEvent.fromDomainToCore(): CoreCalloutEvent =
    CoreCalloutEvent(integration, relativeStartTime, getCoreCallout(callout))

fun getCoreCallout(callout: Callout): CoreCallout =  when(callout) {
    is ConfirmationCallout -> callout.fromDomainToCore()
    else -> throw InvalidClientRequestException()
}
