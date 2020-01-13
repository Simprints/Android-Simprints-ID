package com.simprints.id.activities.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManager
import java.util.*

class DashboardViewModel(val peopleSyncManager: PeopleSyncManager) : ViewModel() {

    val syncCardState: LiveData<DashboardSyncCardState> = peopleSyncManager.getLastSyncState().map {
        DashboardSyncCardState.SyncDefault(Date())
    }
}
