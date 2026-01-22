package com.simprints.core.domain.common

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepParams
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@SerialName("AgeGroup")
data class AgeGroup(
    val startInclusive: Int,
    val endExclusive: Int?,
) : StepParams {
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
