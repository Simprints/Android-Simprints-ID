package com.simprints.infra.config.store.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.AgeGroup

@Keep
internal data class ApiAgeGroup(
    val startInclusive: Int?,
    val endExclusive: Int?,
) {
    fun toDomain() = AgeGroup(startInclusive ?: 0, endExclusive)
}
