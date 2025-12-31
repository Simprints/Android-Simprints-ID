package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.callout.ConfirmationCalloutEventV3
import com.simprints.infra.events.event.domain.models.callout.EnrolmentCalloutEventV3
import com.simprints.infra.events.event.domain.models.callout.EnrolmentLastBiometricsCalloutEventV3
import com.simprints.infra.events.event.domain.models.callout.IdentificationCalloutEventV3
import com.simprints.infra.events.event.domain.models.callout.VerificationCalloutEventV3
import kotlinx.serialization.Serializable

@Keep
@Serializable
@ExcludedFromGeneratedTestCoverageReports("Data class")
internal data class ApiCalloutPayloadV3(
    override val startTime: ApiTimestamp,
    val callout: ApiCallout,
) : ApiEventPayload() {
    constructor(domainPayload: EnrolmentCalloutEventV3.EnrolmentCalloutPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        ApiEnrolmentCalloutV3(
            domainPayload.projectId,
            domainPayload.userId.value,
            domainPayload.moduleId.value,
            domainPayload.metadata,
            domainPayload.biometricDataSource.fromDomainToApi(),
        ),
    )

    constructor(domainPayload: IdentificationCalloutEventV3.IdentificationCalloutPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        ApiIdentificationCalloutV3(
            domainPayload.projectId,
            domainPayload.userId.value,
            domainPayload.moduleId.value,
            domainPayload.metadata,
            domainPayload.biometricDataSource.fromDomainToApi(),
        ),
    )

    constructor(domainPayload: VerificationCalloutEventV3.VerificationCalloutPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        ApiVerificationCalloutV3(
            domainPayload.projectId,
            domainPayload.userId.value,
            domainPayload.moduleId.value,
            domainPayload.metadata,
            domainPayload.verifyGuid,
            domainPayload.biometricDataSource.fromDomainToApi(),
        ),
    )

    constructor(domainPayload: ConfirmationCalloutEventV3.ConfirmationCalloutPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        ApiConfirmationCalloutV3(
            domainPayload.selectedGuid,
            domainPayload.sessionId,
            domainPayload.metadata,
        ),
    )

    constructor(domainPayload: EnrolmentLastBiometricsCalloutEventV3.EnrolmentLastBiometricsCalloutPayload) : this(
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
        TokenKeyType.ExternalCredential -> "callout.externalCredential"
        TokenKeyType.Unknown -> null
    }
}
