package com.simprints.feature.setup

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@SerialName("SetupResult")
data class SetupResult(
    val isSuccess: Boolean,
) : StepResult
