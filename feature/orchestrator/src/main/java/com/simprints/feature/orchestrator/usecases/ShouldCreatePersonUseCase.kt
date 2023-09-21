package com.simprints.feature.orchestrator.usecases

import com.simprints.face.capture.FaceCaptureResult
import com.simprints.feature.orchestrator.steps.Step
import com.simprints.feature.orchestrator.steps.StepId
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.orchestration.data.ActionRequest
import javax.inject.Inject

internal class ShouldCreatePersonUseCase @Inject constructor() {

    operator fun invoke(
            actionRequest: ActionRequest?,
            modalities: Set<GeneralConfiguration.Modality>,
            results: List<Step>
    ): Boolean {
        if (actionRequest !is ActionRequest.FlowAction) {
            return false
        }

        val faceCaptureComplete = modalities.contains(GeneralConfiguration.Modality.FACE)
            && results.filter { it.id == StepId.FACE_CAPTURE }.all { it.result is FaceCaptureResult }

        // TODO handle fingerprint
        val fingerprintComplete = true

        return faceCaptureComplete && fingerprintComplete
    }
}
