package com.simprints.face.controllers.core.events

import com.simprints.face.controllers.core.events.model.*
import com.simprints.face.controllers.core.events.model.EventType.*
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.tools.ignoreException
import kotlinx.coroutines.runBlocking

class FaceSessionEventsManagerImpl(private val sessionRepository: SessionRepository) : FaceSessionEventsManager {

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
            REFUSAL_RESPONSE -> (event as RefusalEvent).fromDomainToCore()
            FACE_CAPTURE -> null //(event as FaceCaptureEvent).fromDomainToCore()
            ONE_TO_ONE_MATCH -> (event as OneToOneMatchEvent).fromDomainToCore()
            ONE_TO_MANY_MATCH -> (event as OneToManyMatchEvent).fromDomainToCore()
            REFUSAL -> (event as RefusalEvent).fromDomainToCore()
            PERSON_CREATION -> (event as PersonCreationEvent).fromDomainToCore()
        }
}
