package com.simprints.core.tools.time

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepParams
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class Timestamp(
    val ms: Long,
    val isTrustworthy: Boolean = false,
    val msSinceBoot: Long? = null,
) : Comparable<Timestamp>,
    StepParams {
    override fun compareTo(other: Timestamp): Int = ms.compareTo(other.ms)
}
