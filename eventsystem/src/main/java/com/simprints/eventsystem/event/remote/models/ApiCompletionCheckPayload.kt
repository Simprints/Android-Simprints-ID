package com.simprints.eventsystem.event.remote.models

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.CompletionCheckEvent.CompletionCheckPayload

@Keep
data class ApiCompletionCheckPayload(override val startTime: Long,
                                     override val version: Int,
                                     val completed: Boolean) : ApiEventPayload(ApiEventPayloadType.CompletionCheck, version, startTime) {

    constructor(domainPayload: CompletionCheckPayload):
        this(domainPayload.createdAt, domainPayload.eventVersion, domainPayload.completed)
}

