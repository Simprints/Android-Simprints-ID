package com.simprints.id.data.db.event.remote.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.GuidSelectionEvent.GuidSelectionPayload

@Keep
data class ApiGuidSelectionPayload(override val relativeStartTime: Long,
                                   override val version: Int,
                                   val selectedId: String) : ApiEventPayload(ApiEventPayloadType.GuidSelection, version, relativeStartTime) {

    constructor(domainPayload: GuidSelectionPayload, baseStartTime: Long) :
        this(domainPayload.createdAt - baseStartTime,
            domainPayload.eventVersion,
            domainPayload.selectedId)
}
