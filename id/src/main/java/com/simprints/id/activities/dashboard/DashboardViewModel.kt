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
        DashboardSyncCardState.SyncDefault(Date())
    }

    fun emit() {
        peopleSyncStateLiveData.value = PeopleSyncState("", 0, 0, emptyList(), emptyList())
    }
}
