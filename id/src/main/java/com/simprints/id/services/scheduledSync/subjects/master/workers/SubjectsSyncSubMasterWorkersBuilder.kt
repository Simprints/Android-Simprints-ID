package com.simprints.id.services.scheduledSync.subjects.master.workers

import androidx.work.OneTimeWorkRequest

interface SubjectsSyncSubMasterWorkersBuilder {

    fun buildStartSyncReporterWorker(uniqueSyncID: String): OneTimeWorkRequest
    fun buildEndSyncReporterWorker(uniqueSyncID: String): OneTimeWorkRequest
}
