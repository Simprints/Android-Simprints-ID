package com.simprints.id.data.db.event.remote.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.GuidSelectionEvent.GuidSelectionPayload

@Keep
data class ApiGuidSelectionPayload(override val relativeStartTime: Long,
                                   override val version: Int,
                                   val selectedId: String) : ApiEventPayload(ApiEventPayloadType.GuidSelection, version, relativeStartTime) {

    constructor(domainPayload: GuidSelectionPayload) :
        this(domainPayload.createdAt,
            domainPayload.eventVersion,
            domainPayload.selectedId)
}
