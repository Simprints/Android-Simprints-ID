package com.simprints.id.data.analytics.eventData.models.events

import com.simprints.id.data.analytics.eventData.models.EventType

class GuidSelectionEvent(val relativeStartTime: Long, val selectedId: String) : Event(EventType.GUID_SELECTION)
