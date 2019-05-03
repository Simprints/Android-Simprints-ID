package com.simprints.clientapi.controllers.core.eventData

import com.simprints.clientapi.controllers.core.eventData.model.Event

interface ClientApiSessionEventsManager {

    fun addSessionEvent(sessionEvent: Event)
}
