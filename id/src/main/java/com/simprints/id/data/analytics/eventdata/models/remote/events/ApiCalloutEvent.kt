package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.*
import com.simprints.id.data.analytics.eventdata.models.domain.events.callout.*
import com.simprints.id.data.analytics.eventdata.models.remote.events.ApiCalloutEvent.ApiIntegrationInfo.Companion.fromDomainIntegrationInfo
import com.simprints.id.data.analytics.eventdata.models.remote.events.callout.ApiCallout

@Keep
class ApiCalloutEvent(val relativeStartTime: Long,
                      val integration: ApiIntegrationInfo,
                      val callout: ApiCallout) : ApiEvent(ApiEventType.CALLOUT) {

    constructor(calloutEvent: CalloutEvent) :
        this(calloutEvent.relativeStartTime,
            fromDomainIntegrationInfo(calloutEvent.integration),
            fromDomainCallout(calloutEvent.callout))

    enum class ApiIntegrationInfo {
        ODK, STANDARD;

        companion object {
            fun fromDomainIntegrationInfo(integrationInfo: CalloutEvent.IntegrationInfo) =
                when (integrationInfo) {
                    CalloutEvent.IntegrationInfo.ODK -> ODK
                    CalloutEvent.IntegrationInfo.STANDARD -> STANDARD
                }
        }
    }
}

fun fromDomainCallout(callout: Callout): ApiCallout = when(callout) {
    is EnrolmentCallout -> callout.toApiEnrolmentCallout()
    is IdentificationCallout -> callout.toApiIdentificationCallout()
    is ConfirmationCallout -> callout.toApiConfirmationCallout()
    is VerificationCallout -> callout.toApiVerificationCallout()
    else -> throw Exception() //Stopship
}
