package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.core.tools.time.Timestamp
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiTimestamp(
    val unixMs: Long,
    val isUnixMsTrustworthy: Boolean = false,
    val elapsedSinceBoot: Long? = null,
)

internal fun Timestamp.fromDomainToApi() = ApiTimestamp(ms, isTrustworthy, msSinceBoot)
