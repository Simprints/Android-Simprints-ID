package com.simprints.id.services.scheduledSync.peopleDownSync.workers.downsync

import com.simprints.id.data.db.down_sync_info.domain.DownSyncOperation

interface DownSyncTask {

    suspend fun execute(downSyncOperation: DownSyncOperation,
                        downSyncWorkerProgressReporter: DownSyncWorkerProgressReporter)
}
