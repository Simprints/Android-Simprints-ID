package com.simprints.core.domain.reference

import android.os.Parcelable
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.step.StepParams
import com.simprints.core.domain.step.StepResult
import kotlinx.parcelize.Parcelize

@Parcelize
@ExcludedFromGeneratedTestCoverageReports("Data class with generated code")
data class BiometricTemplateCapture(
    val captureEventId: String,
    val template: BiometricTemplate,
) : StepResult,
    StepParams,
    Parcelable
