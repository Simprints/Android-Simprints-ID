package com.simprints.feature.consent

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@SerialName("ConsentResult")
data class ConsentResult(
    val accepted: Boolean,
) : StepResult
