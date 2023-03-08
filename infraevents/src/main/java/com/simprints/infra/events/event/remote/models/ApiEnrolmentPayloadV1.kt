package com.simprints.infra.events.remote.models

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.EnrolmentEventV1

@Keep
data class ApiEnrolmentPayloadV1(override val startTime: Long,
                                 override val version: Int,
                                 val personId: String) : ApiEventPayload(ApiEventPayloadType.Enrolment, version, startTime) {

    constructor(domainPayload: EnrolmentEventV1.EnrolmentPayload) :
        this(domainPayload.createdAt, domainPayload.eventVersion, domainPayload.personId)
}
