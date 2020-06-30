package com.simprints.id.data.db.event.remote.events.callout

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.callout.ConfirmationCalloutEvent.ConfirmationCalloutPayload
import com.simprints.id.data.db.event.domain.events.callout.EnrolmentCalloutEvent.EnrolmentCalloutPayload
import com.simprints.id.data.db.event.domain.events.callout.EnrolmentLastBiometricsCalloutEvent.EnrolmentLastBiometricsCalloutPayload
import com.simprints.id.data.db.event.domain.events.callout.IdentificationCalloutEvent.IdentificationCalloutPayload
import com.simprints.id.data.db.event.domain.events.callout.VerificationCalloutEvent.VerificationCalloutPayload
import com.simprints.id.data.db.event.remote.events.ApiEvent
import com.simprints.id.data.db.event.remote.events.ApiEventPayload
import com.simprints.id.data.db.event.remote.events.ApiEventPayloadType.CALLOUT
import com.simprints.id.data.db.event.remote.events.fromDomainToApi

@Keep
class ApiCalloutEvent(id: String,
                      labels: List<Event.EventLabel>,
                      payload: EventPayload) :
    ApiEvent(id, labels.fromDomainToApi(), payload.fromDomainToApi()) {

    @Keep
    class ApiCalloutPayload(
        createdAt: Long,
        version: Int,
        val callout: ApiCallout) : ApiEventPayload(CALLOUT, version, createdAt) {

        constructor(domainPayload: EnrolmentCalloutPayload) : this(
            domainPayload.createdAt,
            domainPayload.eventVersion,
            ApiEnrolmentCallout(
                domainPayload.projectId,
                domainPayload.userId,
                domainPayload.moduleId,
                domainPayload.metadata))

        constructor(domainPayload: IdentificationCalloutPayload) : this(
            domainPayload.createdAt,
            domainPayload.eventVersion,
            ApiIdentificationCallout(
                domainPayload.projectId,
                domainPayload.userId,
                domainPayload.moduleId,
                domainPayload.metadata))

        constructor(domainPayload: VerificationCalloutPayload) : this(
            domainPayload.createdAt,
            domainPayload.eventVersion,
            ApiVerificationCallout(
                domainPayload.projectId,
                domainPayload.userId,
                domainPayload.moduleId,
                domainPayload.metadata,
                domainPayload.verifyGuid))

        constructor(domainPayload: ConfirmationCalloutPayload) : this(
            domainPayload.createdAt,
            domainPayload.eventVersion,
            ApiConfirmationCallout(
                domainPayload.selectedGuid,
                domainPayload.sessionId))

        constructor(domainPayload: EnrolmentLastBiometricsCalloutPayload) : this(
            domainPayload.createdAt,
            domainPayload.eventVersion,
            ApiEnrolmentLastBiometricsCallout(
                domainPayload.projectId,
                domainPayload.userId,
                domainPayload.moduleId,
                domainPayload.metadata,
                domainPayload.sessionId))
    }
}

