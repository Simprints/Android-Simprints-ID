package com.simprints.id.orchestrator

import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.face.uniqueId
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.domain.fingerprint.uniqueId
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.core.tools.utils.EncodingUtilsImpl
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.models.PersonCreationEvent
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureBiometricsEvent
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureBiometricsEvent
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.models.fromDomainToModuleApi
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.orchestrator.steps.Step.Result
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.toList
import javax.inject.Inject

class PersonCreationEventHelperImpl(
    private val eventRepository: EventRepository,
    private val timeHelper: TimeHelper,
    private val encodingUtils: EncodingUtils
) : PersonCreationEventHelper {

    @Inject
    constructor(eventRepository: EventRepository, timeHelper: TimeHelper) : this(
        eventRepository,
        timeHelper,
        EncodingUtilsImpl
    )

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
        val fingerprintCaptureBiometricsEvents =
            eventRepository.getEventsFromSession(currentCaptureSessionEvent.id)
                .filterIsInstance<FingerprintCaptureBiometricsEvent>().toList()
        val faceCaptureBiometricsEvents =
            eventRepository.getEventsFromSession(currentCaptureSessionEvent.id)
                .filterIsInstance<FaceCaptureBiometricsEvent>().toList()

        val personCreationEvent = build(
            timeHelper,
            faceCaptureBiometricsEvents,
            fingerprintCaptureBiometricsEvents,
            faceSamples,
            fingerprintSamples
        )
        if (personCreationEvent.hasBiometricData()) {
            eventRepository.addOrUpdateEvent(personCreationEvent)
        }
    }

    fun build(
        timeHelper: TimeHelper,
        faceCaptureBiometricsEvents: List<FaceCaptureBiometricsEvent>,
        fingerprintCaptureBiometricsEvents: List<FingerprintCaptureBiometricsEvent>,
        faceSamplesForPersonCreation: List<FaceSample>?,
        fingerprintSamplesForPersonCreation: List<FingerprintSample>?
    ) = PersonCreationEvent(
        startTime = timeHelper.now(),
        fingerprintCaptureIds = extractFingerprintCaptureEventIdsBasedOnPersonTemplate(
            fingerprintCaptureBiometricsEvents,
            fingerprintSamplesForPersonCreation?.map { encodingUtils.byteArrayToBase64(it.template) }
        ),
        fingerprintReferenceId = fingerprintSamplesForPersonCreation?.uniqueId(),
        faceCaptureIds = extractFaceCaptureEventIdsBasedOnPersonTemplate(
            faceCaptureBiometricsEvents,
            faceSamplesForPersonCreation?.map { encodingUtils.byteArrayToBase64(it.template) }
        ),
        faceReferenceId = faceSamplesForPersonCreation?.uniqueId()
    )

    private fun extractFingerprintCaptureEventIdsBasedOnPersonTemplate(
        captureEvents: List<FingerprintCaptureBiometricsEvent>,
        personTemplates: List<String>?
    ): List<String>? =
        captureEvents
            .filter {
                personTemplates?.contains(it.payload.fingerprint.template) ?: false
            }.map { it.payload.id }
            .ifEmpty { null }

    private fun extractFaceCaptureEventIdsBasedOnPersonTemplate(
        captureEvents: List<FaceCaptureBiometricsEvent>,
        personTemplates: List<String>?
    ): List<String>? =
        captureEvents
            .filter {
                personTemplates?.contains(it.payload.face.template) ?: false
            }.map { it.payload.id }
            .ifEmpty { null }

    private fun PersonCreationEvent.hasBiometricData() =
        payload.fingerprintCaptureIds?.isNotEmpty() == true || payload.faceCaptureIds?.isNotEmpty() == true
}

