package com.simprints.feature.consent

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepResult

@Keep
data class ConsentResult(
    val accepted: Boolean,
) : StepResult
