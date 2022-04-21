package com.simprints.id.orchestrator

import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.face.uniqueId
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.domain.fingerprint.uniqueId
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.eventsystem.event.domain.models.PersonCreationEvent
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureEvent
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureEvent
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureEvent.FingerprintCapturePayload.Result.SKIPPED
import com.simprints.id.data.db.subject.domain.fromDomainToModuleApi
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.orchestrator.steps.Step.Result
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.toList

class PersonCreationEventHelperImpl(
    val eventRepository: com.simprints.eventsystem.event.EventRepository,
    val timeHelper: TimeHelper,
    private val encodingUtils: EncodingUtils
) : PersonCreationEventHelper {

    override suspend fun addPersonCreationEventIfNeeded(steps: List<Result>) {
        val currentSession = eventRepository.getCurrentCaptureSessionEvent()
        val personCreationEventInSession = eventRepository.getEventsFromSession(currentSession.id)
            .filterIsInstance<PersonCreationEvent>().toList()
        // If a personCreationEvent is already in the current session,
        // we don' want to add it again (the capture steps would still be the same)
        if (personCreationEventInSession.isEmpty()) {
            val faceCaptureResponses = steps.filterIsInstance<FaceCaptureResponse>()
            val fingerprintCaptureResponses = steps.filterIsInstance<FingerprintCaptureResponse>()
            val fingerprintSamples = extractFingerprintSamples(fingerprintCaptureResponses)
            val faceSamples = extractFaceSamples(faceCaptureResponses)
            addPersonCreationEvent(fingerprintSamples, faceSamples)
        }
    }

    private fun extractFingerprintSamples(responses: List<FingerprintCaptureResponse>) =
        responses.map {
            it.captureResult.mapNotNull { captureResult ->
                val fingerId = captureResult.identifier
                captureResult.sample?.let { sample ->
                    FingerprintSample(
                        fingerId.fromDomainToModuleApi(),
                        sample.template,
                        sample.templateQualityScore,
                        sample.format.fromDomainToModuleApi()
                    )
                }
            }
        }.flatten()


    private fun extractFaceSamples(responses: List<FaceCaptureResponse>) =
        responses.map {
            it.capturingResult.mapNotNull { captureResult ->
                captureResult.result?.let { sample ->
                    FaceSample(sample.template, sample.format.fromDomainToModuleApi())
                }
            }
        }.flatten()

    private suspend fun addPersonCreationEvent(
        fingerprintSamples: List<FingerprintSample>,
        faceSamples: List<FaceSample>
    ) {
        val currentCaptureSessionEvent = eventRepository.getCurrentCaptureSessionEvent()
        val fingerprintCaptureEvents =
            eventRepository.getEventsFromSession(currentCaptureSessionEvent.id)
                .filterIsInstance<FingerprintCaptureEvent>().toList()
        val faceCaptureEvents = eventRepository.getEventsFromSession(currentCaptureSessionEvent.id)
            .filterIsInstance<FaceCaptureEvent>().toList()

        eventRepository.addOrUpdateEvent(
            build(
                timeHelper,
                faceCaptureEvents,
                fingerprintCaptureEvents,
                faceSamples,
                fingerprintSamples
            )
        )
    }

    fun build(
        timeHelper: TimeHelper,
        faceCaptureEvents: List<FaceCaptureEvent>,
        fingerprintCaptureEvents: List<FingerprintCaptureEvent>,
        faceSamplesForPersonCreation: List<FaceSample>?,
        fingerprintSamplesForPersonCreation: List<FingerprintSample>?
    ) = PersonCreationEvent(
        startTime = timeHelper.now(),
        fingerprintCaptureIds = extractFingerprintCaptureEventIdsBasedOnPersonTemplate(
            fingerprintCaptureEvents,
            fingerprintSamplesForPersonCreation?.map { encodingUtils.byteArrayToBase64(it.template) }
        ),
        fingerprintReferenceId = fingerprintSamplesForPersonCreation?.uniqueId(),
        faceCaptureIds = extractFaceCaptureEventIdsBasedOnPersonTemplate(
            faceCaptureEvents,
            faceSamplesForPersonCreation?.map { encodingUtils.byteArrayToBase64(it.template) }
        ),
        faceReferenceId = faceSamplesForPersonCreation?.uniqueId()
    )

    private fun extractFingerprintCaptureEventIdsBasedOnPersonTemplate(
        captureEvents: List<FingerprintCaptureEvent>,
        personTemplates: List<String>?
    ): List<String>? =
        captureEvents
            .filter {
                personTemplates?.contains(it.payload.fingerprint?.template) ?: false
                    && it.payload.result != SKIPPED
            }.map { it.id }
            .nullIfEmpty()

    private fun extractFaceCaptureEventIdsBasedOnPersonTemplate(
        captureEvents: List<FaceCaptureEvent>,
        personTemplates: List<String>?
    ): List<String>? =
        captureEvents
            .filter {
                personTemplates?.contains(it.payload.face?.template) ?: false
            }.map { it.id }
            .nullIfEmpty()

    private fun List<String>.nullIfEmpty() =
        if (this.isNotEmpty()) {
            this
        } else {
            null
        }
}
