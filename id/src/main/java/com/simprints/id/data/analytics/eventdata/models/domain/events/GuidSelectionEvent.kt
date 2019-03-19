package com.simprints.id.data.analytics.eventdata.models.domain.events

import com.simprints.id.data.analytics.eventdata.models.domain.EventType

class GuidSelectionEvent(val relativeStartTime: Long, val selectedId: String) : Event(EventType.GUID_SELECTION)
