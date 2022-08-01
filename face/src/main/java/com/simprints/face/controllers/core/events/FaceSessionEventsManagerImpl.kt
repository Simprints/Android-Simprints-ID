package com.simprints.face.controllers.core.events

import com.simprints.core.tools.extentions.inBackground
import com.simprints.eventsystem.event.EventRepository
import com.simprints.face.controllers.core.events.model.*
import com.simprints.face.controllers.core.events.model.EventType.*
import com.simprints.id.tools.ignoreException
import kotlinx.coroutines.runBlocking
import com.simprints.eventsystem.event.domain.models.Event as CoreEvent

class FaceSessionEventsManagerImpl(private val eventRepository: EventRepository) :
    FaceSessionEventsManager {

    override fun addEventInBackground(event: Event) {
        fromDomainToCore(event).let {
            inBackground {
                eventRepository.addOrUpdateEvent(it)
            }
        }
    }

    override fun addEvent(event: Event) {
        runBlocking {
            ignoreException {
                fromDomainToCore(event).let {
                    eventRepository.addOrUpdateEvent(it)
                }
            }
        }
    }

    private fun fromDomainToCore(event: Event): CoreEvent =
        when (event.type) {
            FACE_ONBOARDING_COMPLETE -> (event as FaceOnboardingCompleteEvent).fromDomainToCore()
            FACE_FALLBACK_CAPTURE -> (event as FaceFallbackCaptureEvent).fromDomainToCore()
            FACE_CAPTURE_CONFIRMATION -> (event as FaceCaptureConfirmationEvent).fromDomainToCore()
            FACE_CAPTURE -> (event as FaceCaptureEvent).fromDomainToCore()
            FACE_CAPTURE_BIOMETRICS -> (event as FaceCaptureBiometricsEvent).fromDomainToCore()
            ALERT_SCREEN -> (event as AlertScreenEvent).fromDomainToCore()
            ONE_TO_ONE_MATCH -> (event as OneToOneMatchEvent).fromDomainToCore()
            ONE_TO_MANY_MATCH -> (event as OneToManyMatchEvent).fromDomainToCore()
            REFUSAL -> (event as RefusalEvent).fromDomainToCore()
        }

    override suspend fun removeLocationDataFromSession() =
        eventRepository.removeLocationDataFromCurrentSession()

}
