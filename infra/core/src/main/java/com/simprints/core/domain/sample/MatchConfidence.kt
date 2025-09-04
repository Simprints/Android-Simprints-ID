package com.simprints.core.domain.sample

import android.os.Parcelable
import androidx.annotation.Keep
import com.simprints.core.domain.step.StepParams
import com.simprints.core.domain.step.StepResult
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class MatchConfidence(
    val subjectId: String,
    val confidence: Float,
) : StepParams,
    StepResult,
    Parcelable
