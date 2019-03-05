package com.simprints.id.data.analytics.eventdata.models.domain.events

class GuidSelectionEvent(val relativeStartTime: Long, val selectedId: String) : Event(EventType.GUID_SELECTION)
