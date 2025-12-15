package com.simprints.core.domain.sample

import android.os.Parcelable
import androidx.annotation.Keep
import com.simprints.core.domain.step.StepParams
import com.simprints.core.domain.step.StepResult
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class ComparisonResult(
    val subjectId: String,
    val comparisonScore: Float,
) : StepParams,
    StepResult,
    Parcelable
