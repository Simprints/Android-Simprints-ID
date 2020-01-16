package com.simprints.id.activities.dashboard

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManager
import java.util.*

class DashboardViewModel(val peopleSyncManager: PeopleSyncManager) : ViewModel() {

    var syncCardState = MediatorLiveData<DashboardSyncCardState>()

    init {
        syncCardState.addSource(getLiveDataDefaultState()) { value -> syncCardState.setValue(value) }
        syncCardState.addSource(getLiveDataForSyncState()) { value -> syncCardState.setValue(value) }
        syncCardState.addSource(getLiveDataForConnectivityState()) { value -> syncCardState.setValue(value) }
        syncCardState.addSource(getLiveDataForSyncState()) { value -> syncCardState.setValue(value) }

    }

    private fun getLiveDataDefaultState() = MutableLiveData<DashboardSyncCardState>().apply {
        value = DashboardSyncCardState.SyncDefault(Date())
    }

    private fun getLiveDataForSyncState() =
        peopleSyncManager.getLastSyncState().map {
            DashboardSyncCardState.SyncProgress(Date(), 10, 100)
        }

    private fun getLiveDataForConnectivityState() =
        MutableLiveData<DashboardSyncCardState>()


    private fun getLiveDataForModuleState() =
        MutableLiveData<DashboardSyncCardState>()
}
