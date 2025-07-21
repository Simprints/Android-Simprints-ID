package com.simprints.feature.consent

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepParams

@Keep
data class ConsentParams(
    val type: ConsentType,
) : StepParams
