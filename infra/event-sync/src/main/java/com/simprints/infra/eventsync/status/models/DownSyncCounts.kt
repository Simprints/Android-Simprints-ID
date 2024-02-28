package com.simprints.infra.eventsync.status.models

data class DownSyncCounts(
    val count: Int,
    val isLowerBound: Boolean,
)
