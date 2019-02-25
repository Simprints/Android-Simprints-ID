package com.simprints.id.data.analytics.eventData.models.domain.events

import com.simprints.id.data.analytics.eventData.models.domain.EventType
import java.util.*

open class Event(var type: EventType, val id: String = UUID.randomUUID().toString())
