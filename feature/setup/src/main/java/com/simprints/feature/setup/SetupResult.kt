package com.simprints.feature.setup

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepResult

@Keep
data class SetupResult(
    val isSuccess: Boolean,
) : StepResult
