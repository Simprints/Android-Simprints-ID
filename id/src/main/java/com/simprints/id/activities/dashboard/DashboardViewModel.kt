package com.simprints.id.activities.dashboard

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.work.WorkInfo.State.*
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState.*
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.people_sync.down.domain.ModuleSyncScope
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManager
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncState
import com.simprints.id.tools.device.DeviceManager
import java.util.*

class DashboardViewModel(peopleSyncManager: PeopleSyncManager,
                         deviceManager: DeviceManager,
                         private val preferencesManager: PreferencesManager,
                         private val syncScopeRepository: PeopleDownSyncScopeRepository) : ViewModel() {

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
        val lastSyncData = Date()

        val newState = if (isModuleSelectionRequired) {
            SyncNoModules(lastSyncData)
        } else if (isConnected) {
            processNewCardStateBasedOnSyncState(syncState)
        } else {
            SyncOffline(lastSyncData)
        }

        syncCardState.value = newState
    }

    private fun processNewCardStateBasedOnSyncState(syncState: PeopleSyncState?): DashboardSyncCardState =
        syncState?.let {
            val hasSyncRunRecentlyOrIsRunning = it.downSyncStates.isNotEmpty() && it.upSyncStates.isNotEmpty()
            return if (hasSyncRunRecentlyOrIsRunning) {
                processRecentSyncState(it)
            } else {
                SyncDefault(Date())
            }
        } ?: SyncDefault(Date())

    private fun processRecentSyncState(syncState: PeopleSyncState): DashboardSyncCardState {
        val downSyncStates = syncState.downSyncStates
        val upSyncStates = syncState.upSyncStates
        val allSyncStates = downSyncStates + upSyncStates
        return when {
            isSyncCompleted(allSyncStates) -> SyncComplete(Date())
            isSyncConnecting(allSyncStates) -> SyncConnecting(Date(), syncState.progress, syncState.total)
            isSyncFailed(allSyncStates) -> SyncTryAgain(Date())
            else -> SyncProgress(Date(), syncState.progress, syncState.total)
        }
    }

    private fun isSyncFailed(allSyncStates: List<PeopleSyncState.WorkerState>) =
        allSyncStates.all { arrayOf(FAILED, BLOCKED, CANCELLED).contains(it.state) }

    private fun isSyncConnecting(allSyncStates: List<PeopleSyncState.WorkerState>) =
        allSyncStates.any { it.state == ENQUEUED }

    private fun isSyncCompleted(allSyncStates: List<PeopleSyncState.WorkerState>) =
        allSyncStates.all { it.state == SUCCEEDED }


    private fun isModuleSelectionRequired() =
        preferencesManager.selectedModules.isEmpty() && syncScopeRepository.getDownSyncScope() is ModuleSyncScope

    // If we don't know about the connection yet, we default to true
    private fun isConnected() = isConnectedLiveData.value ?: true
}
