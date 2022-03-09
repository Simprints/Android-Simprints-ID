package com.simprints.eventsystem.event.domain

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.EventType

@Keep
data class EventCount(val type: EventType, val count: Int)
