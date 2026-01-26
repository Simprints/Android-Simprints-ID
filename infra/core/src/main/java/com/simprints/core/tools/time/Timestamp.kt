package com.simprints.core.tools.time

import androidx.annotation.Keep
import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

@Keep
@Serializable
data class Timestamp(
    val ms: Long,
    val isTrustworthy: Boolean = false,
    val msSinceBoot: Long? = null,
) : Comparable<Timestamp>,
    JavaSerializable {
    override fun compareTo(other: Timestamp): Int = ms.compareTo(other.ms)
}
