package com.simprints.id.data.analytics.eventData.models.events

import com.simprints.id.data.analytics.eventData.models.EventType
import com.simprints.id.tools.json.SkipSerialisationField
import java.util.*

open class Event(var type: EventType, @SkipSerialisationField val eventId: String = UUID.randomUUID().toString())
