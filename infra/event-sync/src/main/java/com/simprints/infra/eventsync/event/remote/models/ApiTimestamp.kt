package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.core.tools.time.Timestamp

@Keep
internal data class ApiTimestamp(
    val unixMs: Long,
    val isUnixMsTrustworthy: Boolean,
    val elapsedSinceBoot: Long?,
) {

    constructor(timestamp: Timestamp) : this(
        timestamp.ms,
        timestamp.isTrustworthy,
        timestamp.msSinceBoot
    )
}
