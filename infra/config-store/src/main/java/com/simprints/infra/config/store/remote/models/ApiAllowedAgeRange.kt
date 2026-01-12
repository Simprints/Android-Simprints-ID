package com.simprints.infra.config.store.remote.models

import androidx.annotation.Keep
import com.simprints.core.domain.common.AgeGroup
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class ApiAllowedAgeRange(
    val startInclusive: Int? = null,
    val endExclusive: Int? = null,
) {
    fun toDomain() = AgeGroup(startInclusive ?: 0, endExclusive)
}
