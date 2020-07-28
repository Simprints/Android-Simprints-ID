package com.simprints.id.data.db.event.remote.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.EnrolmentEvent.EnrolmentPayload

@Keep
class ApiEnrolmentPayload(createdAt: Long,
                          eventVersion: Int,
                          val personId: String) : ApiEventPayload(ApiEventPayloadType.ENROLMENT, eventVersion, createdAt) {

    constructor(domainPayload: EnrolmentPayload) :
        this(domainPayload.createdAt, domainPayload.eventVersion, domainPayload.personId)
}
