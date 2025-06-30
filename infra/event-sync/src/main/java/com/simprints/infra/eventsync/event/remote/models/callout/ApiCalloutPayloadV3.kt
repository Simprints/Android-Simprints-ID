package com.simprints.infra.eventsync.event.remote.models.callout

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.callout.ConfirmationCalloutEventV3.ConfirmationCalloutPayload
import com.simprints.infra.events.event.domain.models.callout.EnrolmentCalloutEventV3.EnrolmentCalloutPayload
import com.simprints.infra.events.event.domain.models.callout.EnrolmentLastBiometricsCalloutEventV3.EnrolmentLastBiometricsCalloutPayload
import com.simprints.infra.events.event.domain.models.callout.IdentificationCalloutEventV3.IdentificationCalloutPayload
import com.simprints.infra.events.event.domain.models.callout.VerificationCalloutEventV3.VerificationCalloutPayload
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayload
import com.simprints.infra.eventsync.event.remote.models.ApiTimestamp
import com.simprints.infra.eventsync.event.remote.models.fromDomainToApi

@Keep
@JsonInclude(Include.NON_NULL)
internal data class ApiCalloutPayloadV3(
    override val startTime: ApiTimestamp,
    val callout: ApiCallout,
) : ApiEventPayload(startTime) {
    constructor(domainPayload: EnrolmentCalloutPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        ApiEnrolmentCalloutV3(
            domainPayload.projectId,
            domainPayload.userId.value,
            domainPayload.moduleId.value,
            domainPayload.metadata,
            domainPayload.biometricDataSource.fromDomainToApi()
        ),
    )

    constructor(domainPayload: IdentificationCalloutPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        ApiIdentificationCalloutV3(
            domainPayload.projectId,
            domainPayload.userId.value,
            domainPayload.moduleId.value,
            domainPayload.metadata,
            domainPayload.biometricDataSource.fromDomainToApi()
        ),
    )

    constructor(domainPayload: VerificationCalloutPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        ApiVerificationCalloutV3(
            domainPayload.projectId,
            domainPayload.userId.value,
            domainPayload.moduleId.value,
            domainPayload.metadata,
            domainPayload.verifyGuid,
            domainPayload.biometricDataSource.fromDomainToApi()
        ),
    )

    constructor(domainPayload: ConfirmationCalloutPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        ApiConfirmationCalloutV3(
            domainPayload.selectedGuid,
            domainPayload.sessionId,
        ),
    )

    constructor(domainPayload: EnrolmentLastBiometricsCalloutPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        ApiEnrolmentLastBiometricsCalloutV3(
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
