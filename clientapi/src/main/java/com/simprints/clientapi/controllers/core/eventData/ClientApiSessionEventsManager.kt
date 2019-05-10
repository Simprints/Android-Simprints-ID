package com.simprints.clientapi.controllers.core.eventData

import com.simprints.clientapi.controllers.core.eventData.model.Event
import io.reactivex.Single

interface ClientApiSessionEventsManager {

    fun createSession(): Single<String>

    fun addSessionEvent(sessionEvent: Event)
}
