package com.simprints.id.services.scheduledSync.people.master.workers

import androidx.work.OneTimeWorkRequest

interface PeopleSyncSubMasterWorkersBuilder {

    fun buildStartSyncReporterWorker(uniqueSyncID: String): OneTimeWorkRequest
    fun buildEndSyncReporterWorker(uniqueSyncID: String): OneTimeWorkRequest
}
