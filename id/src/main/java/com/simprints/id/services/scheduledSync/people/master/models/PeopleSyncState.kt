package com.simprints.id.services.scheduledSync.people.master.models

import androidx.annotation.Keep

@Keep
class PeopleSyncState(val syncId: String,
                      val progress: Int,
                      val total: Int?,
                      val upSyncWorkersInfo: List<SyncWorkerInfo>,
                      val downSyncWorkersInfo: List<SyncWorkerInfo>) {

    data class SyncWorkerInfo(val type: PeopleSyncWorkerType,
                              val state: PeopleSyncWorkerState)

}
