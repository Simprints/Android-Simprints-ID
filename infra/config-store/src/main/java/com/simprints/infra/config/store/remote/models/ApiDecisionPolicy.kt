package com.simprints.infra.config.store.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.DecisionPolicy
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiDecisionPolicy(
    val low: Int,
    val medium: Int,
    val high: Int,
) {
    fun toDomain(): DecisionPolicy = DecisionPolicy(low, medium, high)
}
