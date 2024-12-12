package com.simprints.infra.eventsync.sync.master

import androidx.work.OneTimeWorkRequest
import androidx.work.workDataOf
import com.simprints.infra.eventsync.sync.common.addCommonTagForAllSyncWorkers
import com.simprints.infra.eventsync.sync.common.addTagForEndSyncReporter
import com.simprints.infra.eventsync.sync.common.addTagForMasterSyncId
import com.simprints.infra.eventsync.sync.common.addTagForScheduledAtNow
import com.simprints.infra.eventsync.sync.common.addTagForStartSyncReporter
import javax.inject.Inject

internal class EventSyncSubMasterWorkersBuilder @Inject constructor() {
    fun buildStartSyncReporterWorker(uniqueSyncID: String) = OneTimeWorkRequest
        .Builder(EventStartSyncReporterWorker::class.java)
        .addTagForMasterSyncId(uniqueSyncID)
        .addTagForScheduledAtNow()
        .addCommonTagForAllSyncWorkers()
        .addTagForStartSyncReporter()
        .setInputData(workDataOf(EventStartSyncReporterWorker.SYNC_ID_STARTED to uniqueSyncID))
        .build() as OneTimeWorkRequest

    fun buildEndSyncReporterWorker(
        uniqueSyncID: String,
        downSyncWorkerScopeId: String,
        upSyncWorkerScopeId: String,
    ): OneTimeWorkRequest = OneTimeWorkRequest
        .Builder(EventEndSyncReporterWorker::class.java)
        .addTagForMasterSyncId(uniqueSyncID)
        .addTagForScheduledAtNow()
        .addCommonTagForAllSyncWorkers()
        .addTagForEndSyncReporter()
        .setInputData(
            workDataOf(
                EventEndSyncReporterWorker.SYNC_ID_TO_MARK_AS_COMPLETED to uniqueSyncID,
                EventEndSyncReporterWorker.EVENT_DOWN_SYNC_SCOPE_TO_CLOSE to downSyncWorkerScopeId,
                EventEndSyncReporterWorker.EVENT_UP_SYNC_SCOPE_TO_CLOSE to upSyncWorkerScopeId,
            ),
        ).build() as OneTimeWorkRequest
}
