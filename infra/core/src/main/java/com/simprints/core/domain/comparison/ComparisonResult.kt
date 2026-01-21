package com.simprints.core.domain.comparison

import android.os.Parcelable
import androidx.annotation.Keep
import com.simprints.core.domain.step.StepParams
import com.simprints.core.domain.step.StepResult
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Keep
@Parcelize
@Serializable
data class ComparisonResult(
    val subjectId: String,
    val comparisonScore: Float,
) : StepParams,
    StepResult,
    Parcelable
