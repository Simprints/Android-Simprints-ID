package com.simprints.feature.validatepool

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepResult

@Keep
data class ValidateSubjectPoolResult(
    val isValid: Boolean,
) : StepResult
