package com.simprints.id.data.db.event.remote.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.GuidSelectionEvent.GuidSelectionPayload

@Keep
class ApiGuidSelectionPayload(override val relativeStartTime: Long,
                              override val version: Int,
                              val selectedId: String) : ApiEventPayload(ApiEventPayloadType.GUID_SELECTION, version, relativeStartTime) {

    constructor(domainPayload: GuidSelectionPayload) :
        this(domainPayload.createdAt,
            domainPayload.eventVersion,
            domainPayload.selectedId)
}
