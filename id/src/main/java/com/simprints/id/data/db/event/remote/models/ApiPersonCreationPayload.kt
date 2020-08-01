package com.simprints.id.data.db.event.remote.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.PersonCreationEvent.PersonCreationPayload


@Keep
class ApiPersonCreationPayload(override val relativeStartTime: Long,
                               override val version: Int,
                               val fingerprintCaptureIds: List<String>) : ApiEventPayload(ApiEventPayloadType.PersonCreation, version, relativeStartTime) {

    constructor(domainPayload: PersonCreationPayload) :
        this(domainPayload.createdAt,
            domainPayload.eventVersion,
            domainPayload.fingerprintCaptureIds)
}
