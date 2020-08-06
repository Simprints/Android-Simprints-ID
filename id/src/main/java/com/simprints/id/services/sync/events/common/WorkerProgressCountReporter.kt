package com.simprints.id.services.sync.events.common

interface WorkerProgressCountReporter {

    suspend fun reportCount(count: Int)
}
