package com.simprints.feature.exitform

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@SerialName("ExitFormResult")
data class ExitFormResult(
    val wasSubmitted: Boolean,
    val selectedOption: ExitFormOption? = null,
    val reason: String? = null,
) : StepResult {
    fun submittedOption() = selectedOption?.takeIf { wasSubmitted }
}
