package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep
import java.util.*

@Keep
abstract class Event(var type: EventType, val id: String = UUID.randomUUID().toString())

