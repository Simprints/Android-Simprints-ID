package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EnrolmentEventV2

@Keep
internal data class ApiEnrolmentPayloadV2(
    override val startTime: ApiTimestamp,
    val subjectId: String,
    val projectId: String,
    val moduleId: String,
    val attendantId: String,
    val personCreationEventId: String,
) : ApiEventPayload(startTime) {
    constructor(domainPayload: EnrolmentEventV2.EnrolmentPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.subjectId,
        domainPayload.projectId,
        domainPayload.moduleId.value,
        domainPayload.attendantId.value,
        domainPayload.personCreationEventId,
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = when (tokenKeyType) {
        TokenKeyType.AttendantId -> "attendantId"
        TokenKeyType.ModuleId -> "moduleId"
        TokenKeyType.Unknown -> null
    }
}
