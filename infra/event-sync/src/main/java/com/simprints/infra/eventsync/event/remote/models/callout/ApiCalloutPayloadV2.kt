package com.simprints.infra.eventsync.event.remote.models.callout

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.callout.ConfirmationCalloutEventV2.ConfirmationCalloutPayload
import com.simprints.infra.events.event.domain.models.callout.EnrolmentCalloutEventV2.EnrolmentCalloutPayload
import com.simprints.infra.events.event.domain.models.callout.EnrolmentLastBiometricsCalloutEventV2.EnrolmentLastBiometricsCalloutPayload
import com.simprints.infra.events.event.domain.models.callout.IdentificationCalloutEventV2.IdentificationCalloutPayload
import com.simprints.infra.events.event.domain.models.callout.VerificationCalloutEventV2.VerificationCalloutPayload
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayload
import com.simprints.infra.eventsync.event.remote.models.ApiTimestamp
import com.simprints.infra.eventsync.event.remote.models.fromDomainToApi

@Keep
@JsonInclude(Include.NON_NULL)
internal data class ApiCalloutPayloadV2(
    override val startTime: ApiTimestamp,
    val callout: ApiCallout,
) : ApiEventPayload(startTime) {
    constructor(domainPayload: EnrolmentCalloutPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        ApiEnrolmentCalloutV2(
            domainPayload.projectId,
            domainPayload.userId.value,
            domainPayload.moduleId.value,
            domainPayload.metadata
        ),
    )

    constructor(domainPayload: IdentificationCalloutPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        ApiIdentificationCalloutV2(
            domainPayload.projectId,
            domainPayload.userId.value,
            domainPayload.moduleId.value,
            domainPayload.metadata
        ),
    )

    constructor(domainPayload: VerificationCalloutPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        ApiVerificationCalloutV2(
            domainPayload.projectId,
            domainPayload.userId.value,
            domainPayload.moduleId.value,
            domainPayload.metadata,
            domainPayload.verifyGuid
        ),
    )

    constructor(domainPayload: ConfirmationCalloutPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        ApiConfirmationCalloutV2(
            domainPayload.selectedGuid,
            domainPayload.sessionId,
        ),
    )

    constructor(domainPayload: EnrolmentLastBiometricsCalloutPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        ApiEnrolmentLastBiometricsCalloutV2(
            domainPayload.projectId,
            domainPayload.userId.value,
            domainPayload.moduleId.value,
            domainPayload.metadata,
            domainPayload.sessionId,
        ),
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = when (tokenKeyType) {
        TokenKeyType.AttendantId -> "callout.userId"
        TokenKeyType.ModuleId -> "callout.moduleId"
        TokenKeyType.Unknown -> null
    }
}
