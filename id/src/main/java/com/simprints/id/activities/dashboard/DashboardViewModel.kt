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
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.device.DeviceManager
import timber.log.Timber
import java.util.*

class DashboardViewModel(val peopleSyncManager: PeopleSyncManager,
                         deviceManager: DeviceManager,
                         private val preferencesManager: PreferencesManager,
                         private val syncScopeRepository: PeopleDownSyncScopeRepository,
                         private val cacheSync: PeopleSyncCache,
                         private val timeHelper: TimeHelper) : ViewModel() {

    var syncCardState = MediatorLiveData<DashboardSyncCardState>()
    var hasSyncEverRun: Boolean = false
    private var lastSyncRun: Date? = null

    private var syncStateLiveData = peopleSyncManager.getLastSyncState()
    private var isConnectedLiveData = deviceManager.isConnectedUpdates

    init {
        syncCardState.addSource(syncStateLiveData) { syncState ->
            isConnectedLiveData.value?.let { connectivity ->
                emitCardNewState(connectivity, isModuleSelectionRequired(), syncState)
            }
        }

        syncCardState.addSource(isConnectedLiveData) { connectivity ->
            emitCardNewState(connectivity, isModuleSelectionRequired(), syncStateLiveData.value)
        }
    }


    private fun emitCardNewState(isConnected: Boolean,
                                 isModuleSelectionRequired: Boolean,
                                 syncState: PeopleSyncState?) {
        val lastTimeSyncSucceed = cacheSync.readLastSuccessfulSyncTime()

        val newState = when {
            isModuleSelectionRequired -> SyncNoModules(lastTimeSyncSucceed)
            isConnected && syncState != null -> processNewCardStateBasedOnSyncState(syncState, lastTimeSyncSucceed)
            !isConnected -> SyncOffline(lastTimeSyncSucceed)
            else -> SyncDefault(lastTimeSyncSucceed)
        }

        newState?.let {
            syncCardState.value = it
        }
    }

    private fun processNewCardStateBasedOnSyncState(lastSyncData: PeopleSyncState,
                                                    lastTimeSyncSucceed: Date?): DashboardSyncCardState? {
        val hasSyncRunRecentlyOrIsRunning =
            lastSyncData.downSyncWorkersInfo.isNotEmpty() && lastSyncData.upSyncWorkersInfo.isNotEmpty()

        val newState = if (hasSyncRunRecentlyOrIsRunning) {
            processRecentSyncState(lastSyncData, lastTimeSyncSucceed)
        } else {
            SyncDefault(lastTimeSyncSucceed)
        }

        // We store the time when sync finishes.
        // After TIME_BETWEEN_TWO_SYNCS minutes, sync is launched again
        if (hasSyncFinished(newState)) {
            if (lastSyncRun == null) {
                lastSyncRun = Date()
            }
        } else {
            lastSyncRun = null
        }

        // we want to launch immediately if:
        // - sync has never been triggered from the dashboard (e.g. user opens SPID)
        // and the state is on fail
        // - sync has never been triggered from the dashboard (e.g. user opens SPID)
        // and successfully finished long time ago
        // - sync has never been triggered from the dashboard (e.g. user opens SPID)
        // and there no sync history
        val launchSync =
            isSyncOnNotSucceedAndNeverRunBefore(newState) ||
            isSyncOnCompleteAnRunInBackgroundLongTimeAgo(newState) ||
            !hasSyncRunRecentlyOrIsRunning

        return if (launchSync) {
            peopleSyncManager.sync()
            null
        } else {
            return newState
        }
    }

    private fun processRecentSyncState(syncState: PeopleSyncState,
                                       lastTimeSyncSucceed: Date?): DashboardSyncCardState {
        val downSyncStates = syncState.downSyncWorkersInfo
        val upSyncStates = syncState.upSyncWorkersInfo
        val allSyncStates = downSyncStates + upSyncStates
        return when {
            isSyncCompleted(allSyncStates) -> handleSyncComplete(lastTimeSyncSucceed)
            isSyncProcess(allSyncStates) -> handleSyncProgress(lastTimeSyncSucceed, syncState.progress, syncState.total)
            isSyncConnecting(allSyncStates) -> handleSyncConnecting(lastTimeSyncSucceed, syncState.progress, syncState.total)
            isSyncFailedBecauseCloudIntegration(allSyncStates) -> handleSyncFailed(lastTimeSyncSucceed)
            isSyncFailed(allSyncStates) -> handleSyncTryAgain(lastTimeSyncSucceed)
            else -> handleSyncProgress(lastTimeSyncSucceed, syncState.progress, syncState.total)
        }
    }


    private fun handleSyncConnecting(lastSyncData: Date?, progress: Int, total: Int?) =
        SyncConnecting(lastSyncData, progress, total).also {
            hasSyncEverRun = true
        }

    private fun handleSyncProgress(lastSyncData: Date?, progress: Int, total: Int?) =
        SyncProgress(lastSyncData, progress, total).also {
            hasSyncEverRun = true
        }

    private fun handleSyncFailed(lastSyncData: Date?) =
        SyncFailed(lastSyncData)

    private fun handleSyncTryAgain(lastSyncData: Date?) =
        SyncTryAgain(lastSyncData)

    private fun handleSyncComplete(lastSyncData: Date?) =
        SyncComplete(lastSyncData)

    private fun hasSyncFinished(state: DashboardSyncCardState): Boolean =
        state is SyncComplete || state is SyncTryAgain || state is SyncDefault || state is SyncComplete

    private fun isSyncOnNotSucceedAndNeverRunBefore(state: DashboardSyncCardState): Boolean =
        !hasSyncEverRun && (state is SyncFailed || state is SyncTryAgain)

    private fun isSyncOnCompleteAnRunInBackgroundLongTimeAgo(state: DashboardSyncCardState): Boolean =
        !hasSyncEverRun && (state is SyncComplete) && hasSyncRunLongTimeAgo(state.lastTimeSyncSucceed)

    fun syncIfRequired() {
        val lastSyncState = syncCardState.value
        lastSyncState?.let {
            if (hasSyncFinished(it) && hasSyncRunLongTimeAgo(lastSyncRun)) {
                Timber.d("Sync launched again, last time it run ${TIME_BETWEEN_TWO_SYNCS / (1000 * 60)}m ago")
                peopleSyncManager.sync()
            }
        }
    }

    private fun hasSyncRunLongTimeAgo(lastRun: Date?) =
        timeHelper.msBetweenNowAndTime(lastRun?.time ?: Date().time) > TIME_BETWEEN_TWO_SYNCS


    private fun isSyncFailedBecauseCloudIntegration(allSyncStates: List<SyncWorkerInfo>) =
        allSyncStates.any { it.state is Failed && it.state.failedBecauseCloudIntegration }

    private fun isSyncProcess(allSyncStates: List<SyncWorkerInfo>) =
        allSyncStates.any { it.state is Running }

    private fun isSyncFailed(allSyncStates: List<SyncWorkerInfo>) =
        allSyncStates.any { it.state is Failed || it.state is Blocked || it.state is Cancelled }

    private fun isSyncConnecting(allSyncStates: List<SyncWorkerInfo>) =
        allSyncStates.any { it.state is Enqueued }

    private fun isSyncCompleted(allSyncStates: List<SyncWorkerInfo>) =
        allSyncStates.all { it.state is Succeeded }


    private fun isModuleSelectionRequired() =
        preferencesManager.selectedModules.isEmpty() && syncScopeRepository.getDownSyncScope() is ModuleSyncScope

    companion object {
        private const val ONE_MINUTE = 1000 * 60L
        const val TIME_BETWEEN_TWO_SYNCS = 5 * ONE_MINUTE
    }
}
