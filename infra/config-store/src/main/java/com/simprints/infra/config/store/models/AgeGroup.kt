package com.simprints.infra.config.store.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@Keep
data class AgeGroup(
    @JsonProperty("startInclusive") val startInclusive: Int,
    @JsonProperty("endExclusive") val endExclusive: Int?,
) : Serializable {
    fun isEmpty() = startInclusive == 0 && (endExclusive == null || endExclusive == 0)

    fun includes(age: Int): Boolean {
        val endExclusive = endExclusive ?: Int.MAX_VALUE
        return age in startInclusive until endExclusive
    }

    fun contains(otherRange: AgeGroup): Boolean {
        val thisEndExclusive = this.endExclusive ?: Int.MAX_VALUE
        val otherEndExclusive = otherRange.endExclusive ?: Int.MAX_VALUE
        return startInclusive <= otherRange.startInclusive && otherEndExclusive <= thisEndExclusive
    }
}
