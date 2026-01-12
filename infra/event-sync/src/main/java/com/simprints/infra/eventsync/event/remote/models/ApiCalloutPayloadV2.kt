package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.callout.ConfirmationCalloutEventV2
import com.simprints.infra.events.event.domain.models.callout.EnrolmentCalloutEventV2
import com.simprints.infra.events.event.domain.models.callout.EnrolmentLastBiometricsCalloutEventV2
import com.simprints.infra.events.event.domain.models.callout.IdentificationCalloutEventV2
import com.simprints.infra.events.event.domain.models.callout.VerificationCalloutEventV2
import kotlinx.serialization.Serializable

@Keep
@Serializable
@ExcludedFromGeneratedTestCoverageReports("Data class")
internal data class ApiCalloutPayloadV2(
    override val startTime: ApiTimestamp,
    val callout: ApiCallout,
) : ApiEventPayload() {
    constructor(domainPayload: EnrolmentCalloutEventV2.EnrolmentCalloutPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        ApiEnrolmentCalloutV2(
            domainPayload.projectId,
            domainPayload.userId.value,
            domainPayload.moduleId.value,
            domainPayload.metadata,
        ),
    )

    constructor(domainPayload: IdentificationCalloutEventV2.IdentificationCalloutPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        ApiIdentificationCalloutV2(
            domainPayload.projectId,
            domainPayload.userId.value,
            domainPayload.moduleId.value,
            domainPayload.metadata,
        ),
    )

    constructor(domainPayload: VerificationCalloutEventV2.VerificationCalloutPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        ApiVerificationCalloutV2(
            domainPayload.projectId,
            domainPayload.userId.value,
            domainPayload.moduleId.value,
            domainPayload.metadata,
            domainPayload.verifyGuid,
        ),
    )

    constructor(domainPayload: ConfirmationCalloutEventV2.ConfirmationCalloutPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        ApiConfirmationCalloutV2(
            domainPayload.selectedGuid,
            domainPayload.sessionId,
            domainPayload.metadata,
        ),
    )

    constructor(domainPayload: EnrolmentLastBiometricsCalloutEventV2.EnrolmentLastBiometricsCalloutPayload) : this(
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
        TokenKeyType.ExternalCredential -> "callout.externalCredential"
        TokenKeyType.Unknown -> null
    }
}
