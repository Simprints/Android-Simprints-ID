package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EnrolmentEventV4

@Keep
internal data class ApiEnrolmentPayloadV4(
    override val startTime: ApiTimestamp,
    val subjectId: String,
    val projectId: String,
    val moduleId: String,
    val attendantId: String,
    val biometricReferenceIds: List<String>,
) : ApiEventPayload(startTime) {
    constructor(domainPayload: EnrolmentEventV4.EnrolmentPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.subjectId,
        domainPayload.projectId,
        domainPayload.moduleId.value,
        domainPayload.attendantId.value,
        domainPayload.biometricReferenceIds,
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = when (tokenKeyType) {
        TokenKeyType.AttendantId -> "attendantId"
        TokenKeyType.ModuleId -> "moduleId"
        TokenKeyType.Unknown -> null
    }
}
