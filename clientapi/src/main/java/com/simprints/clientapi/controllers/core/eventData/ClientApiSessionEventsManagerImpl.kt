package com.simprints.clientapi.controllers.core.eventData

import com.simprints.clientapi.controllers.core.eventData.model.CalloutEvent
import com.simprints.clientapi.controllers.core.eventData.model.Event
import com.simprints.clientapi.controllers.core.eventData.model.InvalidIntentEvent
import com.simprints.clientapi.controllers.core.eventData.model.fromDomainToCore
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager

class ClientApiSessionEventsManagerImpl(private val sessionEventsManager: SessionEventsManager) :
    ClientApiSessionEventsManager {

    override fun addSessionEvent(sessionEvent: Event) {
        when (sessionEvent) {
            is InvalidIntentEvent -> sessionEventsManager.addSessionEvent(sessionEvent.fromDomainToCore())
            is CalloutEvent -> sessionEventsManager.addSessionEvent(sessionEvent.fromDomainToCore())
        }
    }
    
}
