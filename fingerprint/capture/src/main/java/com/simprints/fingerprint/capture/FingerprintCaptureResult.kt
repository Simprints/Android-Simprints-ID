package com.simprints.fingerprint.capture

import androidx.annotation.Keep
import com.simprints.core.domain.step.ModalityCaptureStepResult
import com.simprints.core.domain.step.ModalityCaptureStepResultItem

@Keep
data class FingerprintCaptureResult(
    override val referenceId: String,
    override val results: List<ModalityCaptureStepResultItem>,
) : ModalityCaptureStepResult
