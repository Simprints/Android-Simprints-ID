package com.simprints.clientapi.controllers.core.eventData

import com.simprints.clientapi.controllers.core.eventData.model.Event
import com.simprints.clientapi.controllers.core.eventData.model.InvalidIntentEvent
import com.simprints.clientapi.controllers.core.eventData.model.SuspiciousIntentEvent
import com.simprints.clientapi.controllers.core.eventData.model.fromDomainToCore
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import io.reactivex.Single

class ClientApiSessionEventsManagerImpl(private val coreSessionEventsManager: SessionEventsManager) :
    ClientApiSessionEventsManager {

    override fun createSession(): Single<String> {
        val libSimprints = com.simprints.libsimprints.BuildConfig.VERSION_NAME
        return coreSessionEventsManager.createSession(libSimprints).map { it.id }
    }

    override fun addSessionEvent(sessionEvent: Event) {
        when (sessionEvent) {
            is InvalidIntentEvent -> coreSessionEventsManager.addEventInBackground(sessionEvent.fromDomainToCore())
            is SuspiciousIntentEvent -> coreSessionEventsManager.addEventInBackground(sessionEvent.fromDomainToCore())
        }
    }
}
