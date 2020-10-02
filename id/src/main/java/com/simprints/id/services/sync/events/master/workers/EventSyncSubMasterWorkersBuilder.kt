package com.simprints.id.services.sync.events.master.workers

import androidx.work.OneTimeWorkRequest

interface EventSyncSubMasterWorkersBuilder {

    fun buildStartSyncReporterWorker(uniqueSyncID: String): OneTimeWorkRequest
    fun buildEndSyncReporterWorker(uniqueSyncID: String): OneTimeWorkRequest
}
