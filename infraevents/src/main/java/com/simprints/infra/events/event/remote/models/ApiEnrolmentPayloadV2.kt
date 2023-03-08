package com.simprints.infra.events.remote.models

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.EnrolmentEventV2

@Keep
data class ApiEnrolmentPayloadV2(override val startTime: Long,
                                 override val version: Int,
                                 val subjectId: String,
                                 val projectId: String,
                                 val moduleId: String,
                                 val attendantId: String,
                                 val personCreationEventId: String) : ApiEventPayload(ApiEventPayloadType.Enrolment, version, startTime) {

    constructor(domainPayload: EnrolmentEventV2.EnrolmentPayload) : this(
        domainPayload.createdAt,
        domainPayload.eventVersion,
        domainPayload.subjectId,
        domainPayload.projectId,
        domainPayload.moduleId,
        domainPayload.attendantId,
        domainPayload.personCreationEventId)
}
