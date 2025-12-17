package com.simprints.core.domain.capture

import android.os.Parcelable
import androidx.annotation.Keep
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.step.StepParams
import com.simprints.core.domain.step.StepResult
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class BiometricReferenceCapture(
    val referenceId: String,
    val modality: Modality,
    val format: String,
    var templates: List<BiometricTemplateCapture>,
) : StepResult,
    StepParams,
    Parcelable
