package com.simprints.id.services.scheduledSync.people.master.workers

import androidx.work.OneTimeWorkRequest
import androidx.work.workDataOf
import com.simprints.id.services.scheduledSync.people.common.*

class PeopleSyncSubMasterWorkersBuilderImpl: PeopleSyncSubMasterWorkersBuilder {

    override fun buildStartSyncReporterWorker(uniqueSyncID: String) =
        OneTimeWorkRequest.Builder(PeopleStartSyncReporterWorker::class.java)
            .addTagForMasterSyncId(uniqueSyncID)
            .addTagForScheduledAtNow()
            .addCommonTagForAllSyncWorkers()
            .addTagForStartSyncReporter()
            .setInputData(workDataOf(PeopleStartSyncReporterWorker.SYNC_ID_STARTED to uniqueSyncID))
            .build() as OneTimeWorkRequest


    override fun buildEndSyncReporterWorker(uniqueSyncID: String): OneTimeWorkRequest =
        OneTimeWorkRequest.Builder(PeopleEndSyncReporterWorker::class.java)
            .addTagForMasterSyncId(uniqueSyncID)
            .addTagForScheduledAtNow()
            .addCommonTagForAllSyncWorkers()
            .addTagForEndSyncReporter()
            .setInputData(workDataOf(PeopleEndSyncReporterWorker.SYNC_ID_TO_MARK_AS_COMPLETED to uniqueSyncID))
            .build() as OneTimeWorkRequest
}
