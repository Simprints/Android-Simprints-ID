package com.simprints.id.activities.dashboard

import androidx.lifecycle.*
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManager
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncState
import java.util.*

class DashboardViewModel(val peopleSyncManager: PeopleSyncManager) : ViewModel() {

    //private val peopleSyncStateLiveData = peopleSyncManager.getLastSyncState()
    private val peopleSyncStateLiveData = MutableLiveData<PeopleSyncState>()

    val syncCardState = Transformations.map(peopleSyncStateLiveData) {
        DashboardSyncCardState.SyncProgress(Date(), 10, 100)
    }

    fun emit() {
        peopleSyncStateLiveData.value = PeopleSyncState("", 10, 100, emptyList(), emptyList())
    }
}
