package com.simprints.feature.fetchsubject

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepResult

@Keep
data class FetchSubjectResult(
    val found: Boolean,
    val wasOnline: Boolean = false,
) : StepResult
