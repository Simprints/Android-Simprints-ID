package com.simprints.infra.sync

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow

data class SyncResponse(
    val syncCommandJob: Job,
    val syncStatusFlow: StateFlow<SyncStatus>,
)

suspend fun SyncResponse.await() = syncCommandJob.join()
