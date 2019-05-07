package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.*
import com.simprints.id.data.analytics.eventdata.models.domain.events.callout.*
import com.simprints.id.data.analytics.eventdata.models.remote.events.callout.ApiCallout

@Keep
class ApiCalloutEvent(val relativeStartTime: Long,
                      val integration: String?,
                      val callout: ApiCallout) : ApiEvent(ApiEventType.CALLOUT) {

    constructor(calloutEvent: CalloutEvent) : this(calloutEvent.relativeStartTime, calloutEvent.integration, getApiCallout(calloutEvent.callout))
}

fun getApiCallout(callout: Callout): ApiCallout = when(callout) {
    is EnrolmentCallout -> callout.toApiEnrolmentCallout()
    is IdentificationCallout -> callout.toApiIdentificationCallout()
    is ConfirmationCallout -> callout.toApiConfirmationCallout()
    is VerificationCallout -> callout.toApiVerificationCallout()
    else -> throw Exception() //Stopship
}
