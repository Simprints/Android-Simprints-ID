package com.simprints.id.services.scheduledSync.people.down.workers

import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation
import com.simprints.id.services.scheduledSync.people.common.WorkerProgressCountReporter

interface PeopleDownSyncDownloaderTask {

    suspend fun execute(downSyncOperation: PeopleDownSyncOperation,
                        workerId: String,
                        downSyncWorkerProgressReporter: WorkerProgressCountReporter): Int
}
