package com.simprints.eventsystem.event.local

import com.simprints.eventsystem.event.domain.models.Event

internal interface SessionDataCache {

    val eventCache: MutableMap<String, Event>

}
