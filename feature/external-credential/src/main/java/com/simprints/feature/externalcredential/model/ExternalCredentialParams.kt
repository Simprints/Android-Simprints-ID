package com.simprints.feature.externalcredential.model

import androidx.annotation.Keep
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.step.StepParams

@Keep
data class ExternalCredentialParams(
    val subjectId: String?,
    val flowType: FlowType
) : StepParams
