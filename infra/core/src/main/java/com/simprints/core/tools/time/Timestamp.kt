package com.simprints.core.tools.time

data class Timestamp(
    val ms: Long,
    val isTrustworthy: Boolean,
    val msSinceBoot: Long?,
) {

    companion object {

        fun fromLong(longTimestamp: Long) = Timestamp(longTimestamp, false, null)
    }
}
