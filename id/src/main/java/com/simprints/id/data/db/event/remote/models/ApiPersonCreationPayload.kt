package com.simprints.id.data.db.event.remote.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.PersonCreationEvent.PersonCreationPayload


@Keep
class ApiPersonCreationPayload(createdAt: Long,
                               eventVersion: Int,
                               val fingerprintCaptureIds: List<String>) : ApiEventPayload(ApiEventPayloadType.PERSON_CREATION, eventVersion, createdAt) {

    constructor(domainPayload: PersonCreationPayload) :
        this(domainPayload.createdAt,
            domainPayload.eventVersion,
            domainPayload.fingerprintCaptureIds)
}
