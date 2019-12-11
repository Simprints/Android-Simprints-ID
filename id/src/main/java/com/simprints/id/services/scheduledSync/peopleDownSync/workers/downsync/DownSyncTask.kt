package com.simprints.id.services.scheduledSync.peopleDownSync.workers.downsync

import com.simprints.id.data.db.syncscope.domain.DownSyncOperation

interface DownSyncTask {

    suspend fun execute(downSyncOperation: DownSyncOperation,
                        downSyncWorkerProgressReporter: DownSyncWorkerProgressReporter)
}
