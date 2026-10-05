package com.simprints.core.domain.sync

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports

@Keep
@ExcludedFromGeneratedTestCoverageReports("Enum")
enum class SyncFailureReason {
    RELOGIN_REQUIRED,
    CLOUD_INTEGRATION,
    BACKEND_MAINTENANCE,
    TOO_MANY_REQUESTS,
    COMM_CARE_PERMISSION_MISSING,
    UNKNOWN,
}

fun String.toSyncFailureReason(): SyncFailureReason = SyncFailureReason.entries.firstOrNull { it.name == this } ?: SyncFailureReason.UNKNOWN
