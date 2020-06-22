package com.simprints.face.controllers.core.events

import com.simprints.face.controllers.core.events.model.Event
import com.simprints.face.controllers.core.events.model.EventType.*
import com.simprints.face.controllers.core.events.model.OneToManyMatchEvent
import com.simprints.face.controllers.core.events.model.OneToOneMatchEvent
import com.simprints.face.controllers.core.events.model.RefusalEvent
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.tools.ignoreException
import kotlinx.coroutines.runBlocking
import com.simprints.id.data.db.session.domain.models.events.Event as CoreEvent

class FaceSessionEventsManagerImpl(private val sessionRepository: SessionRepository) :
    FaceSessionEventsManager {

    override fun addEventInBackground(event: Event) {
        fromDomainToCore(event)?.let { sessionRepository.addEventToCurrentSessionInBackground(it) }
    }

    override fun addEvent(event: Event) {
        runBlocking {
            ignoreException {
                fromDomainToCore(event)?.let {
                    sessionRepository.updateCurrentSession { currentSession ->
                        currentSession.addEvent(it)
                    }
                }
            }
        }
    }

    private fun fromDomainToCore(event: Event): CoreEvent? =
        when (event.type) {
            FACE_ONBOARDING_COMPLETE -> null // TODO: map to correct core event
            FACE_FALLBACK_CAPTURE -> null // TODO: map to correct core event
            FACE_CAPTURE_CONFIRMATION -> null // TODO: map to correct core event
            FACE_CAPTURE_RETRY -> null // TODO: map to correct core event
            ALERT_SCREEN -> null // TODO: map to correct core event
            FACE_CAPTURE -> null // TODO: map to correct core event
            REFUSAL_RESPONSE -> (event as RefusalEvent).fromDomainToCore()
            ONE_TO_ONE_MATCH -> (event as OneToOneMatchEvent).fromDomainToCore()
            ONE_TO_MANY_MATCH -> (event as OneToManyMatchEvent).fromDomainToCore()
            REFUSAL -> (event as RefusalEvent).fromDomainToCore()
        }
}
