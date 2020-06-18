package com.simprints.id.data.db.session.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.Event
import com.simprints.id.data.db.session.domain.models.events.callout.*
import com.simprints.id.data.db.session.remote.events.callout.*
import java.lang.IllegalArgumentException

@Keep
class ApiCalloutEvent(val relativeStartTime: Long,
                      val callout: ApiCallout) : ApiEvent(ApiEventType.CALLOUT) {

    constructor(enrolmentCalloutEvent: EnrolmentCalloutEvent) :
        this(enrolmentCalloutEvent.relativeStartTime ?: 0,
            fromDomainToApiCallout(enrolmentCalloutEvent))

    constructor(identificationCalloutEvent: IdentificationCalloutEvent) :
        this(identificationCalloutEvent.relativeStartTime ?: 0,
            fromDomainToApiCallout(identificationCalloutEvent))

    constructor(verificationCalloutEvent: VerificationCalloutEvent) :
        this(verificationCalloutEvent.relativeStartTime ?: 0,
            fromDomainToApiCallout(verificationCalloutEvent))

    constructor(confirmationCalloutEvent: ConfirmationCalloutEvent) :
        this(confirmationCalloutEvent.relativeStartTime ?: 0,
            fromDomainToApiCallout(confirmationCalloutEvent))

    constructor(enrolLastBiometricsCalloutEvent: EnrolmentLastBiometricsCalloutEvent) :
        this(enrolLastBiometricsCalloutEvent.relativeStartTime ?: 0,
            fromDomainToApiCallout(enrolLastBiometricsCalloutEvent)
        )
}


fun fromDomainToApiCallout(event: Event): ApiCallout =
    when (event) {
        is EnrolmentCalloutEvent -> with(event) { ApiEnrolmentCallout(projectId, userId, moduleId, metadata) }
        is IdentificationCalloutEvent -> with(event) { ApiIdentificationCallout(projectId, userId, moduleId, metadata) }
        is ConfirmationCalloutEvent -> with(event) { ApiConfirmationCallout(selectedGuid, sessionId) }
        is VerificationCalloutEvent -> with(event) { ApiVerificationCallout(projectId, userId, moduleId, metadata, verifyGuid) }
        is EnrolmentLastBiometricsCalloutEvent -> with(event) { ApiEnrolmentLastBiometricsCallout(projectId, userId, moduleId, metadata, sessionId) }
        else -> throw IllegalArgumentException("Invalid CalloutEvent")
    }
