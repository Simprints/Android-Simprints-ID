package com.simprints.id.services.scheduledSync.subjects.common

interface WorkerProgressCountReporter {

    suspend fun reportCount(count: Int)
}
