package com.simprints.id.services.sync.events.master.workers

import androidx.work.OneTimeWorkRequest
import androidx.work.workDataOf
import com.simprints.id.services.sync.events.common.*

class SubjectsSyncSubMasterWorkersBuilderImpl: SubjectsSyncSubMasterWorkersBuilder {

    override fun buildStartSyncReporterWorker(uniqueSyncID: String) =
        OneTimeWorkRequest.Builder(SubjectsStartSyncReporterWorker::class.java)
            .addTagForMasterSyncId(uniqueSyncID)
            .addTagForScheduledAtNow()
            .addCommonTagForAllSyncWorkers()
            .addTagForStartSyncReporter()
            .setInputData(workDataOf(SubjectsStartSyncReporterWorker.SYNC_ID_STARTED to uniqueSyncID))
            .build() as OneTimeWorkRequest


    override fun buildEndSyncReporterWorker(uniqueSyncID: String): OneTimeWorkRequest =
        OneTimeWorkRequest.Builder(SubjectsEndSyncReporterWorker::class.java)
            .addTagForMasterSyncId(uniqueSyncID)
            .addTagForScheduledAtNow()
            .addCommonTagForAllSyncWorkers()
            .addTagForEndSyncReporter()
            .setInputData(workDataOf(SubjectsEndSyncReporterWorker.SYNC_ID_TO_MARK_AS_COMPLETED to uniqueSyncID))
            .build() as OneTimeWorkRequest
}
