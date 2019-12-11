package com.simprints.id.services.scheduledSync.peopleDownSync.workers.downsync

interface DownSyncWorkerProgressReporter {

    suspend fun reportCount(count: Int)
}
