package com.simprints.feature.selectagegroup

import androidx.annotation.Keep
import com.simprints.core.domain.common.AgeGroup
import com.simprints.core.domain.step.StepResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@SerialName("SelectSubjectAgeGroupResult")
data class SelectSubjectAgeGroupResult(
    val ageGroup: AgeGroup,
) : StepResult
