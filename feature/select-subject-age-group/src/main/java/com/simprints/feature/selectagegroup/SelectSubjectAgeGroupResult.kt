package com.simprints.feature.selectagegroup

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepResult
import com.simprints.infra.config.store.models.AgeGroup

@Keep
data class SelectSubjectAgeGroupResult(
    val ageGroup: AgeGroup,
) : StepResult
