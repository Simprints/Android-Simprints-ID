package com.simprints.feature.orchestrator.usecases

import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.face.uniqueId
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.domain.fingerprint.uniqueId
import com.simprints.core.tools.time.TimeHelper
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.fingerprint.capture.FingerprintCaptureResult
import com.simprints.infra.events.SessionEventRepository
import com.simprints.infra.events.event.domain.models.PersonCreationEvent
import java.io.Serializable
import javax.inject.Inject

internal class CreatePersonEventUseCase @Inject constructor(
    private val eventRepository: SessionEventRepository,
    private val timeHelper: TimeHelper,
) {

    suspend operator fun invoke(results: List<Serializable>) {
        val sessionEvents = eventRepository.getEventsInCurrentSession()

        // If a personCreationEvent is already in the current session,
        // we don' want to add it again (the capture steps would still be the same)
        if (sessionEvents.none { it is PersonCreationEvent }) {
            val faceCaptures = extractFaceCaptures(results)
            val fingerprintCaptures = extractFingerprintCaptures(results)

            val personCreationEvent = build(faceCaptures, fingerprintCaptures)

            if (personCreationEvent.hasBiometricData()) {
                eventRepository.addOrUpdateEvent(personCreationEvent)
            }
        }
    }

    private fun extractFaceCaptures(responses: List<Serializable>) = responses
        .filterIsInstance<FaceCaptureResult>()
        .flatMap { it.results }

    private fun extractFingerprintCaptures(responses: List<Serializable>) = responses
        .filterIsInstance<FingerprintCaptureResult>()
        .flatMap { it.results }

    private fun build(
        faceSamplesForPersonCreation: List<FaceCaptureResult.Item>,
        fingerprintSamplesForPersonCreation: List<FingerprintCaptureResult.Item>,
    ): PersonCreationEvent {
        val fingerprintCaptureIds = fingerprintSamplesForPersonCreation
            .mapNotNull { it.captureEventId }
            .ifEmpty { null }
        val fingerprintReferenceId = fingerprintSamplesForPersonCreation
            .mapNotNull { it.sample }
            .map { FingerprintSample(it.fingerIdentifier, it.template, it.templateQualityScore, it.format) }
            .uniqueId()

        val faceCaptureIds = faceSamplesForPersonCreation
            .mapNotNull { it.captureEventId }
            .ifEmpty { null }
        val faceReferenceId = faceSamplesForPersonCreation
            .mapNotNull { it.sample }
            .map { FaceSample(it.template, it.format) }
            .uniqueId()

        return PersonCreationEvent(
            startTime = timeHelper.now(),
            fingerprintCaptureIds = fingerprintCaptureIds,
            fingerprintReferenceId = fingerprintReferenceId,
            faceCaptureIds = faceCaptureIds,
            faceReferenceId = faceReferenceId,
        )
    }

    private fun PersonCreationEvent.hasBiometricData() =
        payload.fingerprintCaptureIds?.isNotEmpty() == true || payload.faceCaptureIds?.isNotEmpty() == true
}
