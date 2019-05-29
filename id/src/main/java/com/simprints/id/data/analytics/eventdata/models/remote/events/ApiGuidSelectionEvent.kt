package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.GuidSelectionEvent

@Keep
class ApiGuidSelectionEvent(val relativeStartTime: Long,
                            val selectedId: String) : ApiEvent(ApiEventType.GUID_SELECTION) {

    constructor(guidSelectionEvent: GuidSelectionEvent) :
        this(guidSelectionEvent.relativeStartTime ?: 0, guidSelectionEvent.selectedId)
}
