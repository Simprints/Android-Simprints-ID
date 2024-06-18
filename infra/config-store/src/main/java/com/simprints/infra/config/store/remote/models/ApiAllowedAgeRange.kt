package com.simprints.infra.config.store.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.AgeGroup

@Keep
data class ApiAllowedAgeRange(
    val startInclusive: Int?,
    val endExclusive: Int?,
) {

    fun toDomain() =
    // When allowedAgeRange is disabled the API returns an empty object
        // which is then parsed as {null, null}
        if (startInclusive == null && endExclusive == null) null
        else AgeGroup(startInclusive ?: 0, endExclusive)
}
