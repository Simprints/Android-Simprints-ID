package com.simprints.id.data.db.event.remote.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.CompletionCheckEvent.CompletionCheckPayload

@Keep
data class ApiCompletionCheckPayload(override val relativeStartTime: Long,
                                     override val version: Int,
                                     val completed: Boolean) : ApiEventPayload(ApiEventPayloadType.CompletionCheck, version, relativeStartTime) {

    constructor(domainPayload: CompletionCheckPayload, baseStartTime: Long) :
        this(domainPayload.createdAt - baseStartTime, domainPayload.eventVersion, domainPayload.completed)
}

