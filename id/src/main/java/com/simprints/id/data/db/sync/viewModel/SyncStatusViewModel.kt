package com.simprints.id.data.db.sync.viewModel

import androidx.lifecycle.ViewModel
import androidx.work.WorkManager
import com.simprints.id.data.db.sync.room.SyncStatusDatabase
import com.simprints.id.services.scheduledSync.peopleDownSync.PeopleDownSyncMaster

class SyncStatusViewModel(syncStatusDatabase: SyncStatusDatabase, projectId: String): ViewModel() {

    val syncStatus = syncStatusDatabase.syncStatusModel.getSyncStatus()

    val downSyncWorkStatus = WorkManager.getInstance().getStatusesForUniqueWork("$projectId-${PeopleDownSyncMaster.DOWN_SYNC_WORK_NAME_SUFFIX}")
}
