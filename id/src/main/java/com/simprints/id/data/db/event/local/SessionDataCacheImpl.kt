package com.simprints.id.data.db.event.local

import com.simprints.id.Application
import com.simprints.id.data.db.event.domain.models.Event


class SessionDataCacheImpl(val application: Application) : SessionDataCache {

    override val eventCache: MutableMap<String, Event>
        get() = application.eventCache

}
