package com.simprints.id.data.analytics.eventdata.models.domain.events

import java.util.*

abstract class Event(var type: EventType, val id: String = UUID.randomUUID().toString())

