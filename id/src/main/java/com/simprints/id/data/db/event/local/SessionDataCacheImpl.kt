package com.simprints.id.data.db.event.local

import com.simprints.id.Application
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent


class SessionDataCacheImpl(val application: Application) : Application(), SessionDataCache {

    override var currentSession: SessionCaptureEvent?
        get() = application.currentSessionCache
        set(value) {
            application.currentSessionCache = value
        }

}
