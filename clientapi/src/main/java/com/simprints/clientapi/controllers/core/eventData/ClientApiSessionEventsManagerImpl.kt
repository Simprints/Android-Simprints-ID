package com.simprints.clientapi.controllers.core.eventData

import com.simprints.clientapi.controllers.core.eventData.model.InvalidIntentEvent
import com.simprints.clientapi.controllers.core.eventData.model.fromDomainToCore
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager

class ClientApiSessionEventsManagerImpl(private val sessionEventsManager: SessionEventsManager) :
    ClientApiSessionEventsManager {

    override fun addInvalidSession(invalidIntentEvent: InvalidIntentEvent) {
        sessionEventsManager.addInvalidIntentEvent(invalidIntentEvent.fromDomainToCore())
    }
    
}
