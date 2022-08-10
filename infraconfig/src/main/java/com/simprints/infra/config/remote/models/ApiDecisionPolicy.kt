package com.simprints.infra.config.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.domain.DecisionPolicy

@Keep
internal data class ApiDecisionPolicy(
    val low: Int,
    val medium: Int,
    val high: Int,
) {
    fun toDomain(): DecisionPolicy =
        DecisionPolicy(low, medium, high)
}
