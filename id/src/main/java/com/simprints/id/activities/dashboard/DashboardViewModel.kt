package com.simprints.id.activities.dashboard

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState.*
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.people_sync.down.domain.ModuleSyncScope
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManager
import com.simprints.id.services.scheduledSync.people.master.internal.PeopleSyncCache
import com.simprints.id.services.scheduledSync.people.master.models.PeopleSyncState
import com.simprints.id.services.scheduledSync.people.master.models.PeopleSyncState.SyncWorkerInfo
import com.simprints.id.services.scheduledSync.people.master.models.PeopleSyncWorkerState.*
import com.simprints.id.tools.device.DeviceManager
import java.util.*

class DashboardViewModel(peopleSyncManager: PeopleSyncManager,
                         deviceManager: DeviceManager,
                         private val preferencesManager: PreferencesManager,
                         private val syncScopeRepository: PeopleDownSyncScopeRepository,
                         private val cacheSync: PeopleSyncCache) : ViewModel() {

    var syncCardState = MediatorLiveData<DashboardSyncCardState>()

    private var syncStateLiveData = peopleSyncManager.getLastSyncState()
    private var isConnectedLiveData = deviceManager.isConnectedUpdates

    init {
        syncCardState.addSource(syncStateLiveData) { syncState ->
            emitCardNewState(isConnected(), isModuleSelectionRequired(), syncState)
        }

        syncCardState.addSource(isConnectedLiveData) {
            emitCardNewState(it, isModuleSelectionRequired(), syncStateLiveData.value)
        }

        emitCardNewState(isConnected(), isModuleSelectionRequired(), null)
        peopleSyncManager.sync()
    }


    private fun emitCardNewState(isConnected: Boolean,
                                 isModuleSelectionRequired: Boolean,
                                 syncState: PeopleSyncState?) {
        val lastSyncData = cacheSync.readLastSuccessfulSyncTime()

        val newState = when {
            isModuleSelectionRequired -> SyncNoModules(lastSyncData)
            isConnected -> processNewCardStateBasedOnSyncState(syncState, lastSyncData)
            else -> SyncOffline(lastSyncData)
        }

        syncCardState.value = newState
    }

    private fun processNewCardStateBasedOnSyncState(syncState: PeopleSyncState?,
                                                    lastSyncData: Date?): DashboardSyncCardState =
        syncState?.let {
            val hasSyncRunRecentlyOrIsRunning = it.downSyncWorkersInfo.isNotEmpty() && it.upSyncWorkersInfo.isNotEmpty()
            return if (hasSyncRunRecentlyOrIsRunning) {
                processRecentSyncState(it, lastSyncData)
            } else {
                SyncDefault(lastSyncData)
            }
        } ?: SyncDefault(lastSyncData)

    private fun processRecentSyncState(syncState: PeopleSyncState,
                                       lastSyncData: Date?): DashboardSyncCardState {
        val downSyncStates = syncState.downSyncWorkersInfo
        val upSyncStates = syncState.upSyncWorkersInfo
        val allSyncStates = downSyncStates + upSyncStates
        return when {
            isSyncCompleted(allSyncStates) -> SyncComplete(lastSyncData)
            isSyncConnecting(allSyncStates) -> SyncConnecting(lastSyncData, syncState.progress, syncState.total)
            isSyncFailedBecauseCloudIntegration(allSyncStates) -> SyncFailed(lastSyncData)
            isSyncFailed(allSyncStates) -> SyncTryAgain(lastSyncData)
            else -> SyncProgress(lastSyncData, syncState.progress, syncState.total)
        }
    }

    private fun isSyncFailedBecauseCloudIntegration(allSyncStates: List<SyncWorkerInfo>) =
        allSyncStates.all { it.state is Failed && it.state.failedBecauseCloudIntegration }

    private fun isSyncFailed(allSyncStates: List<SyncWorkerInfo>) =
        allSyncStates.all { it.state is Failed || it.state is Blocked || it.state is Cancelled }

    private fun isSyncConnecting(allSyncStates: List<SyncWorkerInfo>) =
        allSyncStates.any { it.state is Enqueued }

    private fun isSyncCompleted(allSyncStates: List<SyncWorkerInfo>) =
        allSyncStates.all { it.state is Succeeded }


    private fun isModuleSelectionRequired() =
        preferencesManager.selectedModules.isEmpty() && syncScopeRepository.getDownSyncScope() is ModuleSyncScope

    // If we don't know about the connection yet, we default to true
    private fun isConnected() = isConnectedLiveData.value ?: true
}
