package com.simprints.feature.selectagegroup

import androidx.annotation.Keep
import com.simprints.core.domain.common.AgeGroup
import com.simprints.core.domain.step.StepResult

@Keep
data class SelectSubjectAgeGroupResult(
    val ageGroup: AgeGroup,
) : StepResult
