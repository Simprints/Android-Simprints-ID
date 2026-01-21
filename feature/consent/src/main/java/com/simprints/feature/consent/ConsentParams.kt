package com.simprints.feature.consent

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepParams
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@SerialName("ConsentParams")
data class ConsentParams(
    val consentType: ConsentType,
) : StepParams
