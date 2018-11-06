package com.simprints.id.data.db.sync.viewModel

import androidx.lifecycle.ViewModel
import com.simprints.id.data.db.sync.room.SyncStatusDatabase

class SyncStatusViewModel(syncStatusDatabase: SyncStatusDatabase): ViewModel() {

    val syncStatus = syncStatusDatabase.syncStatusModel.getSyncStatus()
}
