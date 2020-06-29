package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.callout.ConfirmationCalloutEvent.ConfirmationCalloutPayload
import com.simprints.id.data.db.event.domain.events.callout.EnrolmentCalloutEvent.EnrolmentCalloutPayload
import com.simprints.id.data.db.event.domain.events.callout.EnrolmentLastBiometricsCalloutEvent.EnrolmentLastBiometricsCalloutPayload
import com.simprints.id.data.db.event.domain.events.callout.IdentificationCalloutEvent.IdentificationCalloutPayload
import com.simprints.id.data.db.event.domain.events.callout.VerificationCalloutEvent.VerificationCalloutPayload
import com.simprints.id.data.db.event.remote.events.callout.ApiCallout

@Keep
class ApiCalloutEvent(id: String,
                      labels: List<Event.EventLabel>,
                      payload: EventPayload) :
    ApiEvent(id, labels.fromDomainToApi(), payload.fromDomainToApi()) {

    @Keep
    class ApiCalloutPayload(val callout: ApiCallout) : ApiEventPayload(ApiEventPayloadType.CALLOUT) {

        constructor(domainPayload: EnrolmentCalloutPayload) :
            this(domainPayload.fromDomainToApi() as ApiCallout)

        constructor(domainPayload: IdentificationCalloutPayload) :
            this(domainPayload.fromDomainToApi() as ApiCallout)

        constructor(domainPayload: VerificationCalloutPayload) :
            this(domainPayload.fromDomainToApi() as ApiCallout)

        constructor(domainPayload: ConfirmationCalloutPayload) :
            this(domainPayload.fromDomainToApi() as ApiCallout)

        constructor(domainPayload: EnrolmentLastBiometricsCalloutPayload) :
            this(domainPayload.fromDomainToApi() as ApiCallout)
    }
}

