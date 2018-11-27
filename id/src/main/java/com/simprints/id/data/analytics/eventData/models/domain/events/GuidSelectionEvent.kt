package com.simprints.id.data.analytics.eventData.models.domain.events

import com.simprints.id.data.analytics.eventData.models.domain.EventType

class GuidSelectionEvent(val relativeStartTime: Long, val selectedId: String) : Event(EventType.GUID_SELECTION)
