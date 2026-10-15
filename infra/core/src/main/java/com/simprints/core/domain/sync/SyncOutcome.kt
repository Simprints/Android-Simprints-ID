package com.simprints.core.domain.sync

import androidx.annotation.Keep
import com.simprints.core.tools.time.Timestamp

@Keep
data class SyncOutcome(
    val timestamp: Timestamp,
    val failure: SyncFailureReason?,
) {
    val succeeded: Boolean get() = failure == null
}
