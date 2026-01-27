package com.simprints.infra.sync

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow

data class SyncResponse(
    val syncCommandJob: Job,
    val syncStatusFlow: StateFlow<SyncStatus>,
)

@ExcludedFromGeneratedTestCoverageReports("There is no complex business logic to test")
suspend fun SyncResponse.await() = syncCommandJob.join()
