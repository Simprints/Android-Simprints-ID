package com.simprints.feature.orchestrator.usecases

import com.simprints.face.capture.FaceCaptureResult
import com.simprints.feature.orchestrator.steps.Step
import com.simprints.feature.orchestrator.steps.StepId
import com.simprints.fingerprint.capture.FingerprintCaptureResult
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.events.SessionEventRepository
import com.simprints.infra.events.event.domain.models.PersonCreationEvent
import com.simprints.infra.logging.Simber
import com.simprints.infra.orchestration.data.ActionRequest
import javax.inject.Inject

internal class ShouldCreatePersonUseCase @Inject constructor(
    private val eventRepository: SessionEventRepository,
) {

    suspend operator fun invoke(
        actionRequest: ActionRequest?,
        modalities: Set<GeneralConfiguration.Modality>,
        results: List<Step>
    ): Boolean {
        if (actionRequest !is ActionRequest.FlowAction &&
            actionRequest !is ActionRequest.EnrolLastBiometricActionRequest) {
            return false
        }

        if (modalities.isEmpty()) {
            Simber.e("Modalities are empty")
            return false
        }

        val faceCaptureResults = results.filter { it.id == StepId.FACE_CAPTURE }
        val fingerprintCaptureResults = results.filter { it.id == StepId.FINGERPRINT_CAPTURE }
        val faceCaptureIsScheduled = faceCaptureResults.isNotEmpty()
        val fingerprintCaptureIsScheduled = fingerprintCaptureResults.isNotEmpty()
        val faceCaptureIsComplete = faceCaptureResults.all { it.result is FaceCaptureResult }
        val fingerprintCaptureIsComplete = fingerprintCaptureResults.all { it.result is FingerprintCaptureResult }

        // Neither face nor fingerprint capture is scheduled so no captures for a PersonCreation event
        if (!faceCaptureIsScheduled && !fingerprintCaptureIsScheduled) {
            return false
        }

        // There are scheduled captures but not all of them are complete
        if (!faceCaptureIsComplete || !fingerprintCaptureIsComplete) {
            return false
        }

        val sessionEvents = eventRepository.getEventsInCurrentSession()
        val personCreationEvents = sessionEvents.filterIsInstance<PersonCreationEvent>()
        // We already have the maximum number of PersonCreation events (2) in the session
        if (personCreationEvents.size > 1) {
            return false
        }

        val currentPersonCreationEvent = personCreationEvents.firstOrNull()
        // If all scheduled capture steps are complete and we don't yet have a PersonCreation event,
        // we should create one
        if (currentPersonCreationEvent == null) {
            return true
        }

        // If we have a PersonCreation event but it's missing a reference that was scheduled for capture
        // we should create a new one. This happens when an identification with only one modality is performed
        // (due to matching modalities configuration) and the other modality is scheduled for capture in the
        // following enrol last request
        return (faceCaptureIsScheduled && !currentPersonCreationEvent.hasFaceReference()) ||
                (fingerprintCaptureIsScheduled && !currentPersonCreationEvent.hasFingerprintReference())
    }
}
