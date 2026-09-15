package com.simprints.feature.moduleselector

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@SerialName("ModuleSelectorResult")
data class ModuleSelectorResult(
    val isConfirmed: Boolean,
) : StepResult
