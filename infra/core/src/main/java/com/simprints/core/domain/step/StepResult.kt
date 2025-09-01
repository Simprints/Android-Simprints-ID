package com.simprints.core.domain.step

import androidx.annotation.Keep
import com.simprints.core.domain.sample.CaptureSample
import com.simprints.core.domain.sample.SampleIdentifier
import java.io.Serializable

@Keep
interface StepResult : Serializable

// Capture step results must have same result signature to enable providing  of the
// capture results to the appropriate matching step based on the modality and SDK

@Keep
interface ModalityCaptureStepResult : Serializable {
    val referenceId: String
    val results: List<ModalityCaptureStepResultItem>
}

@Keep
data class ModalityCaptureStepResultItem(
    val captureEventId: String?,
    val identifier: SampleIdentifier,
    val sample: CaptureSample?,
) : StepResult
