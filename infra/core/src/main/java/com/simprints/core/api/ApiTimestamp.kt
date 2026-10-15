package com.simprints.core.api

import androidx.annotation.Keep
import com.simprints.core.tools.time.Timestamp
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class ApiTimestamp(
    val unixMs: Long,
    val isUnixMsTrustworthy: Boolean = false,
    val elapsedSinceBoot: Long? = null,
)

fun Timestamp.fromDomainToApi() = ApiTimestamp(ms, isTrustworthy, msSinceBoot)
