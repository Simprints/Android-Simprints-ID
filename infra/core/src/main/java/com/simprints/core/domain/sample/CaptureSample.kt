package com.simprints.core.domain.sample

import android.os.Parcelable
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.reference.BiometricTemplate
import com.simprints.core.domain.step.StepParams
import com.simprints.core.domain.step.StepResult
import kotlinx.parcelize.Parcelize

@Parcelize
@ExcludedFromGeneratedTestCoverageReports("Data class with generated code")
data class CaptureSample(
    val captureEventId: String,
    val modality: Modality,
    val format: String,
    val template: BiometricTemplate,
) : StepResult,
    StepParams,
    Parcelable
