package com.simprints.feature.validatepool

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@SerialName("ValidateSubjectPoolResult")
data class ValidateSubjectPoolResult(
    val isValid: Boolean,
) : StepResult
