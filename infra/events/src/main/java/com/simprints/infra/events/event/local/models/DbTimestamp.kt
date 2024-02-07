package com.simprints.infra.events.event.local.models

import androidx.annotation.Keep
import com.simprints.core.tools.time.Timestamp

@Keep
internal data class DbTimestamp(
    val unixMs: Long,
    val isTrustworthy: Boolean = false,
    val msSinceBoot: Long? = null,
)

internal fun Timestamp.fromDomainToDb() = DbTimestamp(ms, isTrustworthy, msSinceBoot)

internal fun DbTimestamp.fromDbToDomain() = Timestamp(unixMs, isTrustworthy, msSinceBoot)
