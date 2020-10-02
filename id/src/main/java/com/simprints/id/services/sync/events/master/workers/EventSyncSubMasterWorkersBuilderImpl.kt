package com.simprints.id.services.sync.events.master.workers

import androidx.work.OneTimeWorkRequest
import androidx.work.workDataOf
import com.simprints.id.services.sync.events.common.*

class EventSyncSubMasterWorkersBuilderImpl: EventSyncSubMasterWorkersBuilder {

    override fun buildStartSyncReporterWorker(uniqueSyncID: String) =
        OneTimeWorkRequest.Builder(EventStartSyncReporterWorker::class.java)
            .addTagForMasterSyncId(uniqueSyncID)
            .addTagForScheduledAtNow()
            .addCommonTagForAllSyncWorkers()
            .addTagForStartSyncReporter()
            .setInputData(workDataOf(EventStartSyncReporterWorker.SYNC_ID_STARTED to uniqueSyncID))
            .build() as OneTimeWorkRequest


    override fun buildEndSyncReporterWorker(uniqueSyncID: String): OneTimeWorkRequest =
        OneTimeWorkRequest.Builder(EventEndSyncReporterWorker::class.java)
            .addTagForMasterSyncId(uniqueSyncID)
            .addTagForScheduledAtNow()
            .addCommonTagForAllSyncWorkers()
            .addTagForEndSyncReporter()
            .setInputData(workDataOf(EventEndSyncReporterWorker.SYNC_ID_TO_MARK_AS_COMPLETED to uniqueSyncID))
            .build() as OneTimeWorkRequest
}
