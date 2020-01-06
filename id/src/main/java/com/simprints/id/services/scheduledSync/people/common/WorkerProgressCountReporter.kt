package com.simprints.id.services.scheduledSync.people.common

interface WorkerProgressCountReporter {

    suspend fun reportCount(count: Int)
}
