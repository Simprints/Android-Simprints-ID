package com.simprints.id.data.analytics.eventdata.models.remote.events

import com.simprints.id.data.analytics.eventdata.models.domain.events.GuidSelectionEvent

class ApiGuidSelectionEvent(val relativeStartTime: Long, val selectedId: String) : ApiEvent(ApiEventType.GUID_SELECTION) {

    constructor(guidSelectionEvent: GuidSelectionEvent) :
        this(guidSelectionEvent.relativeStartTime, guidSelectionEvent.selectedId)
}
