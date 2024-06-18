package com.simprints.infra.config.store.remote.models

import com.simprints.infra.config.store.models.AgeGroup

data class ApiAllowedAgeRange(
    val startInclusive: Int?,
    val endExclusive: Int?,
) {
    //TODO(milen): shall we stick to AgeRange name?
    fun toDomain() =
        // When allowedAgeRange is disabled the API returns an empty object
        // which is then parsed as {null, null}
        if (startInclusive == null && endExclusive == null) null
        else AgeGroup(startInclusive ?: 0, endExclusive)
}
