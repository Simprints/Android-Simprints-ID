package com.simprints.infra.config.store.models

data class AgeGroup(
    val startInclusive: Int,
    val endExclusive: Int?,
) {
    fun isEmpty(): Boolean =
        startInclusive == 0 && endExclusive == null
}
