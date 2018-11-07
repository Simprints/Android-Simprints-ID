package com.simprints.id.data.analytics.eventData.models.events

import com.simprints.id.data.analytics.eventData.models.EventType
import java.util.*

open class Event(var type: EventType, val eventId: String = UUID.randomUUID().toString())
