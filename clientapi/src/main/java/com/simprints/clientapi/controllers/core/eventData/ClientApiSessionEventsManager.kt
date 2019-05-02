package com.simprints.clientapi.controllers.core.eventData

import com.simprints.clientapi.controllers.core.eventData.model.InvalidIntentEvent

interface ClientApiSessionEventsManager {

    fun addInvalidSession(invalidIntentEvent: InvalidIntentEvent)
}
