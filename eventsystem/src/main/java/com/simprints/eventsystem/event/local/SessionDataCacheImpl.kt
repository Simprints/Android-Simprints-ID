package com.simprints.eventsystem.event.local

import android.app.Application
import com.simprints.eventsystem.event.domain.models.Event


class SessionDataCacheImpl(val application: Application) : SessionDataCache {

    override val eventCache: MutableMap<String, Event>
        get() = application.eventCache

}
