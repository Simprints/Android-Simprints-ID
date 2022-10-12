package com.simprints.eventsystem.event.local

import com.simprints.eventsystem.EventSystemApplication
import com.simprints.eventsystem.event.domain.models.Event
import javax.inject.Inject


internal class SessionDataCacheImpl @Inject constructor(val application: EventSystemApplication) : SessionDataCache {

    override val eventCache: MutableMap<String, Event>
        get() = application.eventCache

}
