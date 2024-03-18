package com.simprints.feature.orchestrator.usecases

import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.face.uniqueId
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.domain.fingerprint.uniqueId
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.fingerprint.capture.FingerprintCaptureResult
import com.simprints.infra.events.SessionEventRepository
import com.simprints.infra.events.event.domain.models.PersonCreationEvent
import com.simprints.infra.events.event.domain.models.face.FaceCaptureBiometricsEvent
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureBiometricsEvent
import java.io.Serializable
import javax.inject.Inject

internal class CreatePersonEventUseCase @Inject constructor(
    private val eventRepository: SessionEventRepository,
    private val timeHelper: TimeHelper,
    private val encodingUtils: EncodingUtils,
) {

    suspend operator fun invoke(results: List<Serializable>) {
        val sessionEvents = eventRepository.getEventsInCurrentSession()

        // If a personCreationEvent is already in the current session,
        // we don' want to add it again (the capture steps would still be the same)
        if (sessionEvents.none { it is PersonCreationEvent }) {
            val faceCaptureBiometricsEvents = sessionEvents.filterIsInstance<FaceCaptureBiometricsEvent>()
            val fingerprintCaptureBiometricsEvents = sessionEvents.filterIsInstance<FingerprintCaptureBiometricsEvent>()

            val faceSamples = extractFaceSamples(results)
            val fingerprintSamples = extractFingerprintSamples(results)

            val personCreationEvent = build(
                faceCaptureBiometricsEvents,
                fingerprintCaptureBiometricsEvents,
                faceSamples,
                fingerprintSamples
            )
            if (personCreationEvent.hasBiometricData()) {
                eventRepository.addOrUpdateEvent(personCreationEvent)
            }
        }
    }

    private fun extractFaceSamples(responses: List<Serializable>) = responses
        .filterIsInstance<FaceCaptureResult>()
        .flatMap { it.results }
        .mapNotNull { it.sample }
        .map { FaceSample(it.template, it.format) }

    private fun extractFingerprintSamples(responses: List<Serializable>) = responses
        .filterIsInstance<FingerprintCaptureResult>()
        .flatMap { it.results }
        .mapNotNull { it.sample }
        .map { FingerprintSample(it.fingerIdentifier, it.template, it.templateQualityScore, it.format) }

    private fun build(
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
    ): List<String>? = captureEvents
        .filter { personTemplates?.contains(it.payload.fingerprint.template) == true }
        .map { it.payload.id }
        .ifEmpty { null }

    private fun extractFaceCaptureEventIdsBasedOnPersonTemplate(
        captureEvents: List<FaceCaptureBiometricsEvent>,
        personTemplates: List<String>?
    ): List<String>? = captureEvents
        .filter { personTemplates?.contains(it.payload.face.template) == true }
        .map { it.payload.id }
        .ifEmpty { null }

    private fun PersonCreationEvent.hasBiometricData() =
        payload.fingerprintCaptureIds?.isNotEmpty() == true || payload.faceCaptureIds?.isNotEmpty() == true
}
