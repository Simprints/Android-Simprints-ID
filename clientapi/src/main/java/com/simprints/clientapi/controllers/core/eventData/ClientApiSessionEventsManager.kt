package com.simprints.clientapi.controllers.core.eventData

import com.simprints.clientapi.controllers.core.eventData.model.Event
import io.reactivex.Completable

interface ClientApiSessionEventsManager {

    fun createSession(): Completable
    fun addSessionEvent(sessionEvent: Event)
}
