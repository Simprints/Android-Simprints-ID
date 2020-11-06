package com.simprints.id.data.db.event.remote.models.callout

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.id.data.db.event.domain.models.callout.ConfirmationCalloutEvent.ConfirmationCalloutPayload
import com.simprints.id.data.db.event.domain.models.callout.EnrolmentCalloutEvent.EnrolmentCalloutPayload
import com.simprints.id.data.db.event.domain.models.callout.EnrolmentLastBiometricsCalloutEvent.EnrolmentLastBiometricsCalloutPayload
import com.simprints.id.data.db.event.domain.models.callout.IdentificationCalloutEvent.IdentificationCalloutPayload
import com.simprints.id.data.db.event.domain.models.callout.VerificationCalloutEvent.VerificationCalloutPayload
import com.simprints.id.data.db.event.remote.models.ApiEventPayload
import com.simprints.id.data.db.event.remote.models.ApiEventPayloadType.Callout

@Keep
@JsonInclude(Include.NON_NULL)
data class ApiCalloutPayload(
    override val startTime: Long,
    override val version: Int,
    val callout: ApiCallout) : ApiEventPayload(Callout, version, startTime) {

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
