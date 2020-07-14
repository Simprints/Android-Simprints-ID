package com.simprints.face.controllers.core.events

import com.simprints.core.tools.extentions.inBackground
import com.simprints.face.controllers.core.events.model.*
import com.simprints.face.controllers.core.events.model.EventType.*
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.tools.ignoreException
import kotlinx.coroutines.runBlocking
import com.simprints.id.data.db.event.domain.events.Event as CoreEvent

class FaceSessionEventsManagerImpl(private val eventRepository: EventRepository) : FaceSessionEventsManager {

    override fun addEventInBackground(event: Event) {
        fromDomainToCore(event)?.let {
            inBackground {
                eventRepository.addEvent(it)
            }
        }
    }

    override fun addEvent(event: Event) {
        runBlocking {
            ignoreException {
                fromDomainToCore(event)?.let {
                    eventRepository.addEvent(it)
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
