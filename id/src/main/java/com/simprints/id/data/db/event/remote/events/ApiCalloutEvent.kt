package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.callout.*
import com.simprints.id.data.db.event.domain.events.callout.ConfirmationCalloutEvent.ConfirmationCalloutPayload
import com.simprints.id.data.db.event.domain.events.callout.EnrolmentCalloutEvent.EnrolmentCalloutPayload
import com.simprints.id.data.db.event.domain.events.callout.EnrolmentLastBiometricsCalloutEvent.EnrolmentLastBiometricsCalloutPayload
import com.simprints.id.data.db.event.domain.events.callout.IdentificationCalloutEvent.IdentificationCalloutPayload
import com.simprints.id.data.db.event.domain.events.callout.VerificationCalloutEvent.VerificationCalloutPayload
import com.simprints.id.data.db.event.remote.events.callout.*

@Keep
class ApiCalloutEvent(val relativeStartTime: Long,
                      val callout: ApiCallout) : ApiEvent(ApiEventType.CALLOUT) {

    constructor(enrolmentCalloutEvent: EnrolmentCalloutEvent) :
        this((enrolmentCalloutEvent.payload as EnrolmentCalloutPayload).creationTime,
            fromDomainToApiCallout(enrolmentCalloutEvent))

    constructor(identificationCalloutEvent: IdentificationCalloutEvent) :
        this((identificationCalloutEvent.payload as IdentificationCalloutPayload).creationTime,
            fromDomainToApiCallout(identificationCalloutEvent))

    constructor(verificationCalloutEvent: VerificationCalloutEvent) :
        this((verificationCalloutEvent.payload as VerificationCalloutPayload).creationTime,
            fromDomainToApiCallout(verificationCalloutEvent))

    constructor(confirmationCalloutEvent: ConfirmationCalloutEvent) :
        this((confirmationCalloutEvent.payload as ConfirmationCalloutPayload).creationTime,
            fromDomainToApiCallout(confirmationCalloutEvent))

    constructor(enrolLastBiometricsCalloutEvent: EnrolmentLastBiometricsCalloutEvent) :
        this((enrolLastBiometricsCalloutEvent.payload as EnrolmentLastBiometricsCalloutPayload).creationTime,
            fromDomainToApiCallout(enrolLastBiometricsCalloutEvent)
        )
}


fun fromDomainToApiCallout(event: Event): ApiCallout =
    when (event.payload) {
        is EnrolmentCalloutPayload -> with(event.payload) { ApiEnrolmentCallout(projectId, userId, moduleId, metadata) }
        is IdentificationCalloutPayload -> with(event.payload) { ApiIdentificationCallout(projectId, userId, moduleId, metadata) }
        is ConfirmationCalloutPayload -> with(event.payload) { ApiConfirmationCallout(selectedGuid, sessionId) }
        is VerificationCalloutPayload -> with(event.payload) { ApiVerificationCallout(projectId, userId, moduleId, metadata, verifyGuid) }
        is EnrolmentLastBiometricsCalloutPayload -> with(event.payload) { ApiEnrolmentLastBiometricsCallout(projectId, userId, moduleId, metadata, sessionId) }
        else -> throw IllegalArgumentException("Invalid CalloutEvent")
    }
