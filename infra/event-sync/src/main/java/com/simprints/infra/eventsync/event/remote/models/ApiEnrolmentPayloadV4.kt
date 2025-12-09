package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EnrolmentEventV4

@Keep
@ExcludedFromGeneratedTestCoverageReports("Data class")
internal data class ApiEnrolmentPayloadV4(
    override val startTime: ApiTimestamp,
    val subjectId: String,
    val projectId: String,
    val moduleId: String,
    val attendantId: String,
    val biometricReferenceIds: List<String>,
    val externalCredentialIds: List<String>,
) : ApiEventPayload(startTime) {
    constructor(domainPayload: EnrolmentEventV4.EnrolmentPayload) : this(
        startTime = domainPayload.startTime.fromDomainToApi(),
        subjectId = domainPayload.subjectId,
        projectId = domainPayload.projectId,
        moduleId = domainPayload.moduleId.value,
        attendantId = domainPayload.attendantId.value,
        biometricReferenceIds = domainPayload.biometricReferenceIds,
        externalCredentialIds = domainPayload.externalCredentialIds,
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = when (tokenKeyType) {
        TokenKeyType.AttendantId -> "attendantId"
        TokenKeyType.ModuleId -> "moduleId"
        else -> null
    }
}
