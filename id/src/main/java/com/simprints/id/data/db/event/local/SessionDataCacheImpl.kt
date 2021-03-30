package com.simprints.id.data.db.event.local

import com.simprints.id.Application
import com.simprints.id.data.db.event.domain.models.Event


class SessionDataCacheImpl(val application: Application) : SessionDataCache {

    override val eventCache: MutableSet<Event>
        get() = application.eventCache

}
