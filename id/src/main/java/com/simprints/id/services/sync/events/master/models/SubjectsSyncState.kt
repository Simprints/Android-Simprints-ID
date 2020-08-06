package com.simprints.id.services.sync.events.master.models

import androidx.annotation.Keep

@Keep
data class SubjectsSyncState(val syncId: String,
                             val progress: Int,
                             val total: Int?,
                             val upSyncWorkersInfo: List<SyncWorkerInfo>,
                             val downSyncWorkersInfo: List<SyncWorkerInfo>) {

    data class SyncWorkerInfo(val type: SubjectsSyncWorkerType,
                              val state: SubjectsSyncWorkerState)

}
