package com.simprints.core.tools.time

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class Timestamp(
    val ms: Long,
    val isTrustworthy: Boolean = false,
    val msSinceBoot: Long? = null,
) : Comparable<Timestamp>,
    Serializable {
    override fun compareTo(other: Timestamp): Int = ms.compareTo(other.ms)
}
