package com.simprints.id.data.db.event.domain

import com.simprints.id.data.db.event.domain.models.EventType


data class EventCount(val type: EventType, val count: Int)
