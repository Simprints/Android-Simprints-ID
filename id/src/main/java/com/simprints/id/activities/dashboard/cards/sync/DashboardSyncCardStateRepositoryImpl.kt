package com.simprints.id.activities.dashboard.cards.sync

import androidx.lifecycle.MediatorLiveData
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState.*
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.people_sync.down.domain.ModuleSyncScope
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.people.common.SYNC_LOG_TAG
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManager
import com.simprints.id.services.scheduledSync.people.master.internal.PeopleSyncCache
import com.simprints.id.services.scheduledSync.people.master.models.PeopleSyncState
import com.simprints.id.services.scheduledSync.people.master.models.PeopleSyncWorkerState
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.device.DeviceManager
import kotlinx.coroutines.delay
import timber.log.Timber
import java.util.*

class DashboardSyncCardStateRepositoryImpl(val peopleSyncManager: PeopleSyncManager,
                                           val deviceManager: DeviceManager,
                                           private val preferencesManager: PreferencesManager,
                                           private val syncScopeRepository: PeopleDownSyncScopeRepository,
                                           private val cacheSync: PeopleSyncCache,
                                           private val timeHelper: TimeHelper) : DashboardSyncCardStateRepository {

    override val syncCardStateLiveData = MediatorLiveData<DashboardSyncCardState>()

    private var syncStateLiveData = peopleSyncManager.getLastSyncState()
    private var isConnectedLiveData = deviceManager.isConnectedLiveData

    private var lastTimeSyncRun: Date? = null

    private val lastTimeSyncSucceed
        get() = cacheSync.readLastSuccessfulSyncTime()

    private var isUIBoundToSources = false

    private fun emitNewCardState(isConnected: Boolean,
                                 isModuleSelectionRequired: Boolean,
                                 syncState: PeopleSyncState?) {

        val syncRunningAndInfoNotReadyYet = syncState == null && syncCardStateLiveData.value is SyncConnecting
        val syncNoRunningAndInfoNotReadyYet = syncState == null && syncCardStateLiveData.value !is SyncConnecting

        when {
            isModuleSelectionRequired -> SyncNoModules(lastTimeSyncSucceed)
            !isConnected -> SyncOffline(lastTimeSyncSucceed)
            syncRunningAndInfoNotReadyYet -> SyncConnecting(lastTimeSyncSucceed, 0, null)
            syncNoRunningAndInfoNotReadyYet -> SyncDefault(lastTimeSyncSucceed)
            syncState == null -> SyncDefault(null) //Useless after the 2 above - just to satisfy nullability in the else
            else -> processRecentSyncState(syncState)
        }.let { newState ->

            if (syncState != null && isSyncRunning(syncState.downSyncWorkersInfo + syncState.upSyncWorkersInfo)) {
                lastTimeSyncRun = Date()
            }

            updateDashboardCardState(newState)
        }
    }

    private fun processRecentSyncState(syncState: PeopleSyncState): DashboardSyncCardState {

        val downSyncStates = syncState.downSyncWorkersInfo
        val upSyncStates = syncState.upSyncWorkersInfo
        val allSyncStates = downSyncStates + upSyncStates
        return when {
            isThereNotSyncHistory(allSyncStates) -> SyncDefault(lastTimeSyncSucceed)
            isSyncCompleted(allSyncStates) -> SyncComplete(lastTimeSyncSucceed)
            isSyncProcess(allSyncStates) -> SyncProgress(lastTimeSyncSucceed, syncState.progress, syncState.total)
            isSyncConnecting(allSyncStates) -> SyncConnecting(lastTimeSyncSucceed, syncState.progress, syncState.total)
            isSyncFailedBecauseCloudIntegration(allSyncStates) -> SyncFailed(lastTimeSyncSucceed)
            isSyncFailed(allSyncStates) -> SyncTryAgain(lastTimeSyncSucceed)
            else -> SyncProgress(lastTimeSyncSucceed, syncState.progress, syncState.total)
        }
    }

    override suspend fun syncIfRequired() {
        if (syncCardStateLiveData.value == null) {
            updateDashboardCardState(SyncConnecting(lastTimeSyncSucceed, 0, null))
        }

        var delayBeforeObserve = 0L
        if (shouldForceOneTimeSync()) {
            Timber.tag(SYNC_LOG_TAG).d("Re-launching one time sync")
            peopleSyncManager.sync()
            delayBeforeObserve = 1000
        }

        if (!isUIBoundToSources) {
            isUIBoundToSources = true
            delay(delayBeforeObserve)
            bindUIToSync()
        }
    }

    private fun shouldForceOneTimeSync(): Boolean {
        val isRunning = syncStateLiveData.value?.let { isSyncRunning(it.downSyncWorkersInfo + it.upSyncWorkersInfo) } ?: false
        val lastUpdate = lastTimeSyncRun ?: lastTimeSyncSucceed

        return !isRunning && (lastUpdate == null || timeHelper.msBetweenNowAndTime(lastUpdate.time) > MAX_TIME_BEFORE_SYNC_AGAIN)
    }

    private fun bindUIToSync() {
        syncCardStateLiveData.addSource(isConnectedLiveData) { connectivity ->
            emitNewCardState(
                connectivity,
                isModuleSelectionRequired(),
                syncStateLiveData.value)
        }

        syncCardStateLiveData.addSource(syncStateLiveData) { syncState ->
            emitNewCardState(
                isConnected(),
                isModuleSelectionRequired(),
                syncState)
        }
    }

    private fun updateDashboardCardState(newState: DashboardSyncCardState) {
        Timber.tag(SYNC_LOG_TAG).d("new dashboard state: $newState")
        syncCardStateLiveData.value = newState
    }

    private fun isSyncFailedBecauseCloudIntegration(allSyncStates: List<PeopleSyncState.SyncWorkerInfo>) =
        allSyncStates.any { it.state is PeopleSyncWorkerState.Failed && it.state.failedBecauseCloudIntegration }

    private fun isThereNotSyncHistory(allSyncStates: List<PeopleSyncState.SyncWorkerInfo>) = allSyncStates.isEmpty()

    private fun isSyncProcess(allSyncStates: List<PeopleSyncState.SyncWorkerInfo>) =
        allSyncStates.any { it.state is PeopleSyncWorkerState.Running }

    private fun isSyncFailed(allSyncStates: List<PeopleSyncState.SyncWorkerInfo>) =
        allSyncStates.any { it.state is PeopleSyncWorkerState.Failed || it.state is PeopleSyncWorkerState.Blocked || it.state is PeopleSyncWorkerState.Cancelled }

    private fun isSyncConnecting(allSyncStates: List<PeopleSyncState.SyncWorkerInfo>) =
        allSyncStates.any { it.state is PeopleSyncWorkerState.Enqueued }

    private fun isSyncCompleted(allSyncStates: List<PeopleSyncState.SyncWorkerInfo>) =
        allSyncStates.all { it.state is PeopleSyncWorkerState.Succeeded }


    private fun isSyncRunning(allSyncStates: List<PeopleSyncState.SyncWorkerInfo>) =
        isSyncProcess(allSyncStates) || isSyncConnecting(allSyncStates)


    private fun isModuleSelectionRequired() =
        preferencesManager.selectedModules.isEmpty() && syncScopeRepository.getDownSyncScope() is ModuleSyncScope

    private fun isConnected() = isConnectedLiveData.value ?: true

    companion object {
        private const val ONE_MINUTE = 1000 * 60L
        const val MAX_TIME_BEFORE_SYNC_AGAIN = 5 * ONE_MINUTE
    }
}
