package com.simprints.feature.exitform

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepResult

@Keep
data class ExitFormResult(
    val wasSubmitted: Boolean,
    val selectedOption: ExitFormOption? = null,
    val reason: String? = null,
) : StepResult {
    fun submittedOption() = selectedOption?.takeIf { wasSubmitted }
}
