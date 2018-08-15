package com.simprints.id.data.analytics.events.models

class GuidSelectionEvent(val relativeStartTime: Long, val selectdId: String) : Event(EventType.GUID_SELECTION)
