package com.simprints.eventsystem

import com.simprints.core.CoreApplication
import com.simprints.eventsystem.event.domain.models.Event

class EventSystemApplication: CoreApplication() {

    val eventCache: MutableMap<String, Event> = mutableMapOf()

}
