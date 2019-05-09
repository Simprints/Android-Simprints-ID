package com.simprints.clientapi.controllers.core.eventData

import com.simprints.clientapi.controllers.core.eventData.model.*
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import io.reactivex.Completable

class ClientApiSessionEventsManagerImpl(private val sessionEventsManager: SessionEventsManager) :
    ClientApiSessionEventsManager {

    override fun createSession(): Completable =
        sessionEventsManager.createSession().ignoreElement()

    override fun addSessionEvent(sessionEvent: Event) {
        when (sessionEvent) {
            is InvalidIntentEvent -> sessionEventsManager.addSessionEvent(sessionEvent.fromDomainToCore())
            is SuspiciousIntentEvent -> sessionEventsManager.addSessionEvent(sessionEvent.fromDomainToCore())
        }
    }
}
