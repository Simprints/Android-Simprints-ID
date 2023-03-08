package com.simprints.infra.eventsync.sync.common

interface WorkerProgressCountReporter {

    suspend fun reportCount(count: Int)
}
