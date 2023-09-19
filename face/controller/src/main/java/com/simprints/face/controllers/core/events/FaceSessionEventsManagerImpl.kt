package com.simprints.face.controllers.core.events

import com.simprints.core.ExternalScope
import com.simprints.core.tools.exceptions.ignoreException
import com.simprints.face.controllers.core.events.model.*
import com.simprints.face.controllers.core.events.model.EventType.*
import com.simprints.infra.events.EventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import com.simprints.infra.events.event.domain.models.Event as CoreEvent

class FaceSessionEventsManagerImpl @Inject constructor(
    private val eventRepository: EventRepository,
    @ExternalScope private val externalScope: CoroutineScope
) : FaceSessionEventsManager {

    override fun addEventInBackground(event: Event) {
        fromDomainToCore(event).let {
            externalScope.launch {
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
        }
}
