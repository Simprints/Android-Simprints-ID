package com.simprints.id.data.db.event.remote.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.CompletionCheckEvent.CompletionCheckPayload

@Keep
class ApiCompletionCheckPayload(createdAt: Long,
                                eventVersion: Int,
                                val completed: Boolean) : ApiEventPayload(ApiEventPayloadType.COMPLETION_CHECK, eventVersion, createdAt) {

    constructor(domainPayload: CompletionCheckPayload) :
        this(domainPayload.createdAt, domainPayload.eventVersion, domainPayload.completed)
}

