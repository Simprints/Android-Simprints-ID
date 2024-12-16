package com.simprints.core.tools.time

import androidx.annotation.Keep

@Keep
data class Timestamp(
    val ms: Long,
    val isTrustworthy: Boolean = false,
    val msSinceBoot: Long? = null,
) : Comparable<Timestamp> {
    override fun compareTo(other: Timestamp): Int = ms.compareTo(other.ms)
}
