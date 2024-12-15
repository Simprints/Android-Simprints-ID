package com.simprints.feature.selectagegroup

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.AgeGroup
import java.io.Serializable

@Keep
data class SelectSubjectAgeGroupResult(
    val ageGroup: AgeGroup,
) : Serializable
