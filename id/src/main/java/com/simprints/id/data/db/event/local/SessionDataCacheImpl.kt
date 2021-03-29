package com.simprints.id.data.db.event.local

import com.simprints.id.Application
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent


class SessionDataCacheImpl(val application: Application) : SessionDataCache {

    override val eventCache: MutableList<Event>
        get() = application.eventCache

}
