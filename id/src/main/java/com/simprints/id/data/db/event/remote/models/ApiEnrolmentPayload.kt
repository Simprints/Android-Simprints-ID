package com.simprints.id.data.db.event.remote.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.EnrolmentEvent.EnrolmentPayload

@Keep
data class ApiEnrolmentPayload(override val relativeStartTime: Long,
                               override val version: Int,
                               val personId: String) : ApiEventPayload(ApiEventPayloadType.Enrolment, version, relativeStartTime) {

    constructor(domainPayload: EnrolmentPayload) :
        this(domainPayload.createdAt, domainPayload.eventVersion, domainPayload.personId)
}
