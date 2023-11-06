package com.simprints.feature.orchestrator.usecases

import com.simprints.face.capture.FaceCaptureResult
import com.simprints.feature.orchestrator.steps.Step
import com.simprints.feature.orchestrator.steps.StepId
import com.simprints.fingerprint.capture.FingerprintCaptureResult
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.orchestration.data.ActionRequest
import javax.inject.Inject

internal class ShouldCreatePersonUseCase @Inject constructor() {

    operator fun invoke(
        actionRequest: ActionRequest?,
        modalities: Set<GeneralConfiguration.Modality>,
        results: List<Step>
    ): Boolean {
        if (actionRequest !is ActionRequest.FlowAction || modalities.isEmpty()) {
            return false
        }

        val faceComplete = if (modalities.contains(GeneralConfiguration.Modality.FACE)) {
            results.filter { it.id == StepId.FACE_CAPTURE }.all { it.result is FaceCaptureResult }
        } else true

        val fingerprintComplete = if (modalities.contains(GeneralConfiguration.Modality.FINGERPRINT)) {
            results.filter { it.id == StepId.FINGERPRINT_CAPTURE }.all { it.result is FingerprintCaptureResult }
        } else true

        return faceComplete && fingerprintComplete
    }
}
