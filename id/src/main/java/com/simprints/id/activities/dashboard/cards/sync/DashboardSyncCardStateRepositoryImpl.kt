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
    private var isConnectedLiveData = deviceManager.isConnectedUpdates
    private var lastSyncTimeObservedRunning: Date? = null
    private var lastSyncTimeObservedFinishing: Date? = null

    private val lastTimeSyncSucceed
        get() = cacheSync.readLastSuccessfulSyncTime()

    private val hasSyncEverRun
        get() = peopleSyncManager.hasSyncEverRunBefore()

    init {
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

    private fun emitNewCardState(isConnected: Boolean,
                                 isModuleSelectionRequired: Boolean,
                                 syncState: PeopleSyncState?) {

        val newState = when {
            isModuleSelectionRequired -> SyncNoModules(lastTimeSyncSucceed)
            !isConnected -> SyncOffline(lastTimeSyncSucceed)
            else -> processRecentSyncState(syncState)
        }

        newState.let {
            if (isSyncRunning(newState)) {
                lastSyncTimeObservedRunning = Date()
            } else if (hasSyncFinished(newState)) {
                lastSyncTimeObservedFinishing = Date()
            }

            syncCardStateLiveData.value = it
            syncIfRequired()
        }
    }

    private fun processRecentSyncState(syncState: PeopleSyncState?): DashboardSyncCardState {

        val downSyncStates = syncState?.downSyncWorkersInfo ?: emptyList()
        val upSyncStates = syncState?.upSyncWorkersInfo ?: emptyList()
        val allSyncStates = downSyncStates + upSyncStates
        return when {
            !hasSyncEverRun || syncState == null || isThereNotSyncHistory(allSyncStates) -> SyncDefault(lastTimeSyncSucceed)
            isSyncCompleted(allSyncStates) -> SyncComplete(lastTimeSyncSucceed)
            isSyncProcess(allSyncStates) -> SyncProgress(lastTimeSyncSucceed, syncState.progress, syncState.total)
            isSyncConnecting(allSyncStates) -> SyncConnecting(lastTimeSyncSucceed, syncState.progress, syncState.total)
            isSyncFailedBecauseCloudIntegration(allSyncStates) -> SyncFailed(lastTimeSyncSucceed)
            isSyncFailed(allSyncStates) -> SyncTryAgain(lastTimeSyncSucceed)
            else -> SyncProgress(lastTimeSyncSucceed, syncState.progress, syncState.total)
        }
    }

    private fun isSyncRunning(state: DashboardSyncCardState): Boolean =
        state is SyncProgress || state is SyncConnecting

    private fun hasSyncFinished(state: DashboardSyncCardState): Boolean =
        state is SyncTryAgain || state is SyncFailed || state is SyncComplete

    override fun syncIfRequired() {
        val lastSyncState = syncCardStateLiveData.value
        lastSyncState?.let {

            // Cases when we want to force a one time sync
            if (shouldForceOneTimeSync(it)) {

                Timber.tag(SYNC_LOG_TAG).d("Re-launching one time sync")
                syncCardStateLiveData.value = SyncConnecting(null, 0, null)
                lastSyncTimeObservedRunning = Date()
                peopleSyncManager.sync()
            }
        }
    }

    private fun shouldForceOneTimeSync(lastSyncState: DashboardSyncCardState): Boolean {
        val hasSyncEverBeenObservedRunning = lastSyncTimeObservedRunning != null
        /**
         * Sync never run before.
         * use case: after the login
         */
        return !hasSyncEverRun ||
            /**
             * Sync has never been observed running in the dashboard and last sync (in background) failed.
             * use case: user opens the dashboard and the sync failed before in background
             */
            (!hasSyncEverBeenObservedRunning && hasLastSyncFailed(lastSyncState)) ||
            /**
             * Sync has finished and last time it run quite long time ago.
             * use case: user does sync in the dashboard and then it leaves the dashboard open
             */
            (hasSyncFinished(lastSyncState) && hasSyncRunLongTimeAgo())

    }

    private fun hasLastSyncFailed(state: DashboardSyncCardState): Boolean =
        state is SyncFailed || state is SyncTryAgain

    private fun hasSyncRunLongTimeAgo(): Boolean {
        val timeSinceLastObservedSyncCompleted = timeHelper.msBetweenNowAndTime(lastSyncTimeObservedFinishing?.time ?: Date().time)
        val timeSinceLastSuccess = timeHelper.msBetweenNowAndTime(lastTimeSyncSucceed?.time ?: Date().time)

        // if sync has never been observed running (e.g. user opens SPID), then we fall back using
        // the last time when sync completed
        return timeSinceLastObservedSyncCompleted > MAX_TIME_BEFORE_SYNC_AGAIN ||
            (timeSinceLastSuccess > MAX_TIME_BEFORE_SYNC_AGAIN)
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


    private fun isModuleSelectionRequired() =
        preferencesManager.selectedModules.isEmpty() && syncScopeRepository.getDownSyncScope() is ModuleSyncScope

    private fun isConnected() = isConnectedLiveData.value ?: true

    companion object {
        private const val ONE_MINUTE = 1000 * 60L
        const val MAX_TIME_BEFORE_SYNC_AGAIN = 5 * ONE_MINUTE
    }
}
