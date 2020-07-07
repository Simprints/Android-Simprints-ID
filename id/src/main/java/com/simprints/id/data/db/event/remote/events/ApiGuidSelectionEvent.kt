package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.GuidSelectionEvent
import com.simprints.id.data.db.event.domain.events.GuidSelectionEvent.GuidSelectionPayload

@Keep
class ApiGuidSelectionEvent(val relativeStartTime: Long,
                            val selectedId: String) : ApiEvent(ApiEventType.GUID_SELECTION) {

    constructor(guidSelectionEvent: GuidSelectionEvent) :
        this((guidSelectionEvent.payload as GuidSelectionPayload).creationTime, guidSelectionEvent.payload.selectedId)
}
