package com.simprints.id.data.db.event.remote.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.GuidSelectionEvent.GuidSelectionPayload

@Keep
class ApiGuidSelectionPayload(createdAt: Long,
                              eventVersion: Int,
                              val selectedId: String) : ApiEventPayload(ApiEventPayloadType.GUID_SELECTION, eventVersion, createdAt) {

    constructor(domainPayload: GuidSelectionPayload) :
        this(domainPayload.createdAt,
            domainPayload.eventVersion,
            domainPayload.selectedId)
}
