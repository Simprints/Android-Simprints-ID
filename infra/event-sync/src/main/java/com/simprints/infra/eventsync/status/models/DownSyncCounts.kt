package com.simprints.infra.eventsync.status.models

data class DownSyncCounts(
    val toCreate: Int,
    val toDelete: Int
)
