package com.simprints.core.domain.common

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.core.domain.step.StepParams
import java.io.Serializable

@Keep
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
data class AgeGroup(
    @JsonProperty("startInclusive") val startInclusive: Int,
    @JsonProperty("endExclusive") val endExclusive: Int?,
) : StepParams,
    Serializable {
    // prevents Jackson isEmpty unwanted serialization bug,
    // see https://stackoverflow.com/questions/69616587/why-does-jackson-add-an-empty-false-into-the-json
    @JsonIgnore
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
