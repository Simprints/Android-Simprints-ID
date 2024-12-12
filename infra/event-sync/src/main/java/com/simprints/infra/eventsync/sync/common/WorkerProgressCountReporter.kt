package com.simprints.infra.eventsync.sync.common

internal interface WorkerProgressCountReporter {
    suspend fun reportCount(
        count: Int,
        maxCount: Int?,
    )
}
