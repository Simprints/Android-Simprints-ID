package com.simprints.eventsystem.event.domain

import com.simprints.eventsystem.event.domain.models.EventType


data class EventCount(val type: EventType, val count: Int)
