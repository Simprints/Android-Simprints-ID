package com.simprints.id.services.sync.subjects.common

interface WorkerProgressCountReporter {

    suspend fun reportCount(count: Int)
}
